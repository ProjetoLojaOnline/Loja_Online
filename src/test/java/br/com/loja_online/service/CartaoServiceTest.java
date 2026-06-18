package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.repository.CartaoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    private Cartao cartao;

    @BeforeEach
    void setUp() {
        cartao = CartaoBuilder.padrao().buildModel();
    }

    @Test
    @DisplayName("criarCartaoDeveRetornarCartaoSalvo")
    void criarCartaoDeveRetornarCartaoSalvo() {
        when(cartaoRepository.save(any(Cartao.class))).thenReturn(cartao);

        Cartao resultado = cartaoService.criarCartao(cartao);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(cartao.getId());
        assertThat(resultado.getNomeCartao()).isEqualTo(cartao.getNomeCartao());
        verify(cartaoRepository).save(cartao);
    }

    @Test
    @DisplayName("getCartaoPorIdDeveRetornarCartaoQuandoExiste")
    void getCartaoPorIdDeveRetornarCartaoQuandoExiste() {
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));

        Cartao resultado = cartaoService.getCartaoPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCartaoPorIdDeveLancarExceptionQuandoNaoExiste")
    void getCartaoPorIdDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.getCartaoPorId(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deletarCartaoDeveRemoverQuandoExiste")
    void deletarCartaoDeveRemoverQuandoExiste() {
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));

        cartaoService.deletarCartao(1L);

        verify(cartaoRepository).delete(cartao);
    }

    @Test
    @DisplayName("deletarCartaoDeveLancarExceptionQuandoNaoExiste")
    void deletarCartaoDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.deletarCartao(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("atualizarCartaoDeveAtualizarCamposQuandoExiste")
    void atualizarCartaoDeveAtualizarCamposQuandoExiste() {
        Cartao atualizado = CartaoBuilder.padrao().comCvv(456).buildModel();
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));
        when(cartaoRepository.save(any(Cartao.class))).thenReturn(atualizado);

        Cartao resultado = cartaoService.atualizarCartao(1L, atualizado);

        assertThat(resultado.getCvv()).isEqualTo(456);
        verify(cartaoRepository).save(any(Cartao.class));
    }

    @Test
    @DisplayName("atualizarCartaoDeveLancarExceptionQuandoNaoExiste")
    void atualizarCartaoDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.atualizarCartao(99L, cartao))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
