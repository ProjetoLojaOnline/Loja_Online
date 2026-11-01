package br.com.loja_online.client;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.com.loja_online.config.VerificacaoCpfProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FAKE: NÃO verifica nada de verdade. Não existe API pública gratuita de CPF e a escolha do provedor real está
 * pendente de decisão do time (issue #140).
 *
 * <p>Aprova qualquer CPF, exceto os configurados em {@code verificacao.cpf.fake.rejeitados} (responde que não
 * existe) e {@code verificacao.cpf.fake.falhas} (simula falha do provedor).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "verificacao.cpf.provedor", havingValue = "fake", matchIfMissing = true)
public class VerificadorCpfFake implements VerificadorCpfClient {

    private final VerificacaoCpfProperties properties;

    @PostConstruct
    void avisar() {
        log.warn("Verificação de CPF usando provedor FAKE: nenhum CPF está sendo verificado de verdade!");
    }

    @Override
    public boolean cpfExiste(String cpf) {
        if (properties.fake().falhas().contains(cpf)) {
            throw new IllegalStateException("Falha simulada do provedor de CPF");
        }
        return !properties.fake().rejeitados().contains(cpf);
    }
}
