package br.com.loja_online.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;

@SuppressWarnings("null")
class CartaoMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCamposQuandoCartaoValido")
    void paraDtoDeveMapearTodosCamposQuandoCartaoValido() {
        Cartao cartao = CartaoBuilder.padrao().buildModel();

        CartaoDTO dto = CartaoMapper.paraDto(cartao);

        assertThat(dto).isNotNull();
        assertThat(dto.getNumeroCartao()).isEqualTo(cartao.getNumeroCartao());
        assertThat(dto.getNomeCartao()).isEqualTo(cartao.getNomeCartao());
        assertThat(dto.getDataValidade()).isEqualTo(cartao.getDataValidade());
        assertThat(dto.getCvv()).isEqualTo(cartao.getCvv());
        assertThat(dto.getDefaultCard()).isEqualTo(cartao.getDefaultCard());
    }

    @Test
    @DisplayName("paraDtoDeveRetornarNullQuandoCartaoNull")
    void paraDtoDeveRetornarNullQuandoCartaoNull() {
        CartaoDTO dto = CartaoMapper.paraDto(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("paraCartaoDeveMapearTodosCamposQuandoDtoValido")
    void paraCartaoDeveMapearTodosCamposQuandoDtoValido() {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        Cartao cartao = CartaoMapper.paraCartao(dto);

        assertThat(cartao.getNumeroCartao()).isEqualTo(dto.getNumeroCartao());
        assertThat(cartao.getNomeCartao()).isEqualTo(dto.getNomeCartao());
        assertThat(cartao.getCvv()).isEqualTo(dto.getCvv());
        assertThat(cartao.getDefaultCard()).isEqualTo(dto.getDefaultCard());
    }
}
