package br.com.loja_online.unit.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.client.VerificadorCpfFake;
import br.com.loja_online.config.VerificacaoCpfProperties;

class VerificadorCpfFakeTest {

    private final VerificadorCpfFake fake = new VerificadorCpfFake(new VerificacaoCpfProperties(
            "fake",
            new VerificacaoCpfProperties.Job(50, Duration.ofMinutes(1), Duration.ofHours(1), Duration.ofDays(7)),
            new VerificacaoCpfProperties.Http(null, Duration.ofSeconds(5), Duration.ofSeconds(60)),
            new VerificacaoCpfProperties.Fake(List.of("52998224725"), List.of("11144477735"))));

    @Test
    @DisplayName("deveAprovarCpfQuandoNaoEstaEmNenhumaLista")
    void deveAprovarCpfQuandoNaoEstaEmNenhumaLista() {
        assertThat(fake.cpfExiste("18575819038")).isTrue();
    }

    @Test
    @DisplayName("deveResponderQueNaoExisteQuandoCpfEstaNaListaDeRejeitados")
    void deveResponderQueNaoExisteQuandoCpfEstaNaListaDeRejeitados() {
        assertThat(fake.cpfExiste("52998224725")).isFalse();
    }

    @Test
    @DisplayName("deveSimularFalhaQuandoCpfEstaNaListaDeFalhas")
    void deveSimularFalhaQuandoCpfEstaNaListaDeFalhas() {
        assertThatThrownBy(() -> fake.cpfExiste("11144477735")).isInstanceOf(IllegalStateException.class);
    }
}
