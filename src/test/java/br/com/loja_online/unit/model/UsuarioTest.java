package br.com.loja_online.unit.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.model.Usuario;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;

class UsuarioTest {

    private static Usuario comStatus(StatusVerificacaoCpf status) {
        return Usuario.builder().statusVerificacaoCpf(status).build();
    }

    @Test
    @DisplayName("deveIniciarComoPendenteTantoPeloBuilderQuantoPeloConstrutor")
    void deveIniciarComoPendenteTantoPeloBuilderQuantoPeloConstrutor() {
        assertThat(Usuario.builder().build().getStatusVerificacaoCpf()).isEqualTo(StatusVerificacaoCpf.PENDENTE);
        assertThat(new Usuario().getStatusVerificacaoCpf()).isEqualTo(StatusVerificacaoCpf.PENDENTE);
    }

    @Test
    @DisplayName("pendentePodeComprarMasNaoPodeVender")
    void pendentePodeComprarMasNaoPodeVender() {
        Usuario usuario = comStatus(StatusVerificacaoCpf.PENDENTE);
        assertThat(usuario.podeComprar()).isTrue();
        assertThat(usuario.podeVender()).isFalse();
    }

    @Test
    @DisplayName("aprovadoPodeComprarEVender")
    void aprovadoPodeComprarEVender() {
        Usuario usuario = comStatus(StatusVerificacaoCpf.APROVADO);
        assertThat(usuario.podeComprar()).isTrue();
        assertThat(usuario.podeVender()).isTrue();
    }

    @Test
    @DisplayName("rejeitadoNaoPodeComprarNemVender")
    void rejeitadoNaoPodeComprarNemVender() {
        Usuario usuario = comStatus(StatusVerificacaoCpf.REJEITADO);
        assertThat(usuario.podeComprar()).isFalse();
        assertThat(usuario.podeVender()).isFalse();
    }
}
