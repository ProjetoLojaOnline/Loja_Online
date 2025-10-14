package br.com.loja_online.mapper;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;

public class ProdutoMapper {

    public static ProdutoDTO paraDto(Produto produto) {
        return new ProdutoDTO(produto.getId(), produto.getNome(), produto.getDescricao(),
                produto.getCategoria(), produto.getQuantidade(), produto.getPreco(), produto.getCor());
    }

    public static Produto paraProduto(ProdutoDTO produtoDTO) {
        return new Produto(produtoDTO.id(), produtoDTO.nome(), produtoDTO.descricao(),
                produtoDTO.categoria(), produtoDTO.quantidade(), produtoDTO.preco(),
                produtoDTO.cor());
    }
}
