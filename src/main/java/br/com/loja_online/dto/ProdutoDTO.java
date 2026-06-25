package br.com.loja_online.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProdutoDTO(
        @Schema(description = "ID gerado automaticamente — enviar null no POST") Integer id,
        @Schema(example = "Tênis Esportivo") @NotBlank String nome,
        @Schema(example = "Tênis para corrida confortável") String descricao,
        @Schema(example = "Calçados") @NotBlank String categoria,
        @Schema(example = "10") @PositiveOrZero Integer quantidade,
        @Schema(example = "199.90") @PositiveOrZero BigDecimal preco,
        @Schema(example = "Preto") String cor) {}
