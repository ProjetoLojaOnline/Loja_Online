package br.com.loja_online.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.loja_online.client.VerificadorCpfClient;
import br.com.loja_online.config.VerificacaoCpfProperties;
import br.com.loja_online.dto.VerificacaoCpfPendenteDTO;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;
import br.com.loja_online.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consulta o provedor externo para os usuários com CPF PENDENTE (e REJEITADO com reverificação vencida) e grava
 * APROVADO ou REJEITADO. Se o provedor falhar, o usuário continua como está e a consulta é reagendada com backoff
 * exponencial: o registro nunca é perdido.
 *
 * <p>A chamada ao provedor acontece fora de transação, para não segurar conexão do banco durante o HTTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacaoCpfService {

    private static final List<StatusVerificacaoCpf> STATUS_A_VERIFICAR =
            List.of(StatusVerificacaoCpf.PENDENTE, StatusVerificacaoCpf.REJEITADO);

    private final UsuarioRepository usuarioRepository;
    private final VerificadorCpfClient verificadorCpfClient;
    private final VerificacaoCpfProperties properties;
    private final Clock clock;

    /** @return quantos usuários foram consultados nesta execução */
    public int processarPendentes() {
        List<VerificacaoCpfPendenteDTO> pendentes = usuarioRepository.findParaVerificarCpf(
                STATUS_A_VERIFICAR,
                clock.instant(),
                PageRequest.of(0, properties.job().lote()));

        for (VerificacaoCpfPendenteDTO pendente : pendentes) {
            try {
                verificar(pendente);
            } catch (RuntimeException e) {
                // Erro inesperado (ex.: banco) em um usuário não pode derrubar o job para os demais
                log.error("Erro inesperado ao verificar o CPF do usuário {}", pendente.id(), e);
            }
        }
        return pendentes.size();
    }

    private void verificar(VerificacaoCpfPendenteDTO pendente) {
        boolean existe;
        try {
            existe = verificadorCpfClient.cpfExiste(pendente.cpf());
        } catch (RuntimeException e) {
            reagendarAposFalha(pendente, e);
            return;
        }

        if (existe) {
            salvar(pendente, StatusVerificacaoCpf.APROVADO, 0, null);
        } else {
            Instant reverificacao = clock.instant().plus(properties.job().reverificarRejeitadosApos());
            salvar(pendente, StatusVerificacaoCpf.REJEITADO, 0, reverificacao);
        }
    }

    private void reagendarAposFalha(VerificacaoCpfPendenteDTO pendente, RuntimeException e) {
        int tentativas = pendente.tentativas() + 1;
        Duration espera = calcularBackoff(tentativas);
        log.warn(
                "Provedor de CPF falhou para o usuário {} (tentativa {}). Nova tentativa em {}: {}",
                pendente.id(),
                tentativas,
                espera,
                e.getMessage());
        // Mantém o status atual: PENDENTE continua PENDENTE, REJEITADO continua REJEITADO
        salvar(pendente, pendente.status(), tentativas, clock.instant().plus(espera));
    }

    /** backoffInicial * 2^(tentativas - 1), limitado a backoffMaximo. */
    Duration calcularBackoff(int tentativas) {
        Duration inicial = properties.job().backoffInicial();
        Duration maximo = properties.job().backoffMaximo();
        int expoente = Math.min(tentativas - 1, 30);
        Duration espera = inicial.multipliedBy(1L << expoente);
        return espera.compareTo(maximo) > 0 ? maximo : espera;
    }

    private void salvar(
            VerificacaoCpfPendenteDTO pendente, StatusVerificacaoCpf status, int tentativas, Instant proxima) {
        int atualizados =
                usuarioRepository.atualizarVerificacaoCpf(pendente.id(), pendente.cpf(), status, tentativas, proxima);
        if (atualizados == 0) {
            log.info("CPF do usuário {} mudou durante a verificação; resultado descartado", pendente.id());
        }
    }
}
