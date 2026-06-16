package br.com.loja_online.mapper;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;
import org.springframework.lang.NonNull;

public class CartaoMapper {

    public static CartaoDTO paraDto(Cartao cartao) {
        return new CartaoDTO(
                cartao.getNumeroCartao(),
                cartao.getNomeCartao(),
                cartao.getDataValidade(),
                cartao.getCvv(),
                cartao.getDefaultCard()
        );
    }

    @SuppressWarnings("null")
    public static @NonNull Cartao paraCartao(@NonNull CartaoDTO dto) {
        return Cartao.builder()
                .numeroCartao(dto.getNumeroCartao())
                .nomeCartao(dto.getNomeCartao())
                .dataValidade(dto.getDataValidade())
                .cvv(dto.getCvv())
                .defaultCard(dto.getDefaultCard())
                .build();
    }
}
