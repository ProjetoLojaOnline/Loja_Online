package br.com.loja_online.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProdutoDTO(Integer id,
                         @NotBlank String nome,
                         String descricao,
                         @NotBlank String categoria,
                         @PositiveOrZero Integer quantidade,
                         @PositiveOrZero BigDecimal preco,
                         String cor) {
}
