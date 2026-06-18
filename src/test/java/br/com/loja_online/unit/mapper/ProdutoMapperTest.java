package br.com.loja_online.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.mapper.ProdutoMapper;
import br.com.loja_online.model.Produto;

class ProdutoMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCampos")
    void paraDtoDeveMapearTodosCampos() {
        Produto produto = ProdutoBuilder.padrao().comId(1).buildModel();

        ProdutoDTO dto = ProdutoMapper.paraDto(produto);

        assertThat(dto.id()).isEqualTo(produto.getId());
        assertThat(dto.nome()).isEqualTo(produto.getNome());
        assertThat(dto.categoria()).isEqualTo(produto.getCategoria());
        assertThat(dto.preco()).isEqualByComparingTo(produto.getPreco());
    }

    @Test
    @DisplayName("paraProdutoDeveMapearTodosCampos")
    void paraProdutoDeveMapearTodosCampos() {
        ProdutoDTO dto = ProdutoBuilder.padrao().comId(3).buildDto();

        Produto produto = ProdutoMapper.paraProduto(dto);

        assertThat(produto.getNome()).isEqualTo(dto.nome());
        assertThat(produto.getCategoria()).isEqualTo(dto.categoria());
        assertThat(produto.getPreco()).isEqualByComparingTo(dto.preco());
    }
}
