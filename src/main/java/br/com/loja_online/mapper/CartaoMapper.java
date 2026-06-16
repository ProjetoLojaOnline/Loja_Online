package br.com.loja_online.mapper;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;

public class CartaoMapper {

    public static CartaoDTO paraDto(Cartao cartao) {
        if (cartao == null) {
            return null;
        }
        return new CartaoDTO(
                cartao.getNumeroCartao(),
                cartao.getNomeCartao(),
                cartao.getDataValidade(),
                cartao.getCvv(),
                cartao.getDefaultCard()
        );
    }

    public static Cartao paraCartao(CartaoDTO dto) {
        if (dto == null) {
            return null;
        }
        return Cartao.builder()
                .numeroCartao(dto.getNumeroCartao())
                .nomeCartao(dto.getNomeCartao())
                .dataValidade(dto.getDataValidade())
                .cvv(dto.getCvv())
                .defaultCard(dto.getDefaultCard())
                .build();
    }
}
