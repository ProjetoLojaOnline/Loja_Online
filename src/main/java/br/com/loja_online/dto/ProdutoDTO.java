package br.com.loja_online.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoDTO(Integer id,
                         @NotBlank String nome,
                         String descricao,
                         @NotBlank String categoria,
                         @PositiveOrZero Integer quantidade,
                         @PositiveOrZero BigDecimal preco,
                         String cor) {
}
