package br.com.loja_online.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import br.com.loja_online.client.VerificadorCpfClient;
import br.com.loja_online.config.VerificacaoCpfProperties;
import br.com.loja_online.dto.VerificacaoCpfPendenteDTO;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.VerificacaoCpfService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class VerificacaoCpfServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-01T12:00:00Z");
    private static final String CPF = "52998224725";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VerificadorCpfClient verificadorCpfClient;

    private VerificacaoCpfService service;

    @BeforeEach
    void setUp() {
        VerificacaoCpfProperties properties = new VerificacaoCpfProperties(
                "fake",
                new VerificacaoCpfProperties.Job(50, Duration.ofMinutes(1), Duration.ofMinutes(10), Duration.ofDays(7)),
                new VerificacaoCpfProperties.Http(null, Duration.ofSeconds(5), Duration.ofSeconds(60)),
                new VerificacaoCpfProperties.Fake(List.of(), List.of()));
        service = new VerificacaoCpfService(
                usuarioRepository, verificadorCpfClient, properties, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    private void comPendentes(VerificacaoCpfPendenteDTO... pendentes) {
        when(usuarioRepository.findParaVerificarCpf(anyList(), eq(AGORA), any(Pageable.class)))
                .thenReturn(List.of(pendentes));
    }

    private static VerificacaoCpfPendenteDTO pendente(Long id, int tentativas) {
        return new VerificacaoCpfPendenteDTO(id, CPF, StatusVerificacaoCpf.PENDENTE, tentativas);
    }

    @Test
    @DisplayName("deveAprovarQuandoProvedorDizQueCpfExiste")
    void deveAprovarQuandoProvedorDizQueCpfExiste() {
        comPendentes(pendente(1L, 2));
        when(verificadorCpfClient.cpfExiste(CPF)).thenReturn(true);

        int processados = service.processarPendentes();

        assertThat(processados).isEqualTo(1);
        verify(usuarioRepository).atualizarVerificacaoCpf(1L, CPF, StatusVerificacaoCpf.APROVADO, 0, null);
    }

    @Test
    @DisplayName("deveRejeitarEAgendarReverificacaoQuandoCpfNaoExiste")
    void deveRejeitarEAgendarReverificacaoQuandoCpfNaoExiste() {
        comPendentes(pendente(1L, 0));
        when(verificadorCpfClient.cpfExiste(CPF)).thenReturn(false);

        service.processarPendentes();

        verify(usuarioRepository)
                .atualizarVerificacaoCpf(1L, CPF, StatusVerificacaoCpf.REJEITADO, 0, AGORA.plus(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("deveManterPendenteEAplicarBackoffQuandoProvedorFalha")
    void deveManterPendenteEAplicarBackoffQuandoProvedorFalha() {
        comPendentes(pendente(1L, 0));
        when(verificadorCpfClient.cpfExiste(CPF)).thenThrow(new IllegalStateException("503"));

        service.processarPendentes();

        verify(usuarioRepository)
                .atualizarVerificacaoCpf(1L, CPF, StatusVerificacaoCpf.PENDENTE, 1, AGORA.plus(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("deveDobrarBackoffACadaFalhaAteOMaximo")
    void deveDobrarBackoffACadaFalhaAteOMaximo() {
        comPendentes(pendente(1L, 2), pendente(2L, 20));
        when(verificadorCpfClient.cpfExiste(CPF)).thenThrow(new IllegalStateException("timeout"));

        service.processarPendentes();

        // 3ª falha: 1min * 2^2 = 4min
        verify(usuarioRepository)
                .atualizarVerificacaoCpf(1L, CPF, StatusVerificacaoCpf.PENDENTE, 3, AGORA.plus(Duration.ofMinutes(4)));
        // 21ª falha: limitado ao máximo de 10min
        verify(usuarioRepository)
                .atualizarVerificacaoCpf(
                        2L, CPF, StatusVerificacaoCpf.PENDENTE, 21, AGORA.plus(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("deveManterRejeitadoQuandoReverificacaoFalha")
    void deveManterRejeitadoQuandoReverificacaoFalha() {
        comPendentes(new VerificacaoCpfPendenteDTO(1L, CPF, StatusVerificacaoCpf.REJEITADO, 0));
        when(verificadorCpfClient.cpfExiste(CPF)).thenThrow(new IllegalStateException("503"));

        service.processarPendentes();

        verify(usuarioRepository)
                .atualizarVerificacaoCpf(1L, CPF, StatusVerificacaoCpf.REJEITADO, 1, AGORA.plus(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("deveContinuarProcessandoOsDemaisQuandoUmUsuarioDaErroInesperado")
    void deveContinuarProcessandoOsDemaisQuandoUmUsuarioDaErroInesperado() {
        comPendentes(pendente(1L, 0), pendente(2L, 0));
        when(verificadorCpfClient.cpfExiste(CPF)).thenReturn(true);
        when(usuarioRepository.atualizarVerificacaoCpf(eq(1L), any(), any(), anyInt(), isNull()))
                .thenThrow(new RuntimeException("banco fora"));

        int processados = service.processarPendentes();

        assertThat(processados).isEqualTo(2);
        verify(usuarioRepository).atualizarVerificacaoCpf(2L, CPF, StatusVerificacaoCpf.APROVADO, 0, null);
    }
}
