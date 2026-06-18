package br.com.loja_online.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

public record EnderecoDTO(
        @Schema(example = "Rua das Flores") @NotBlank() String logradouro,
        @Schema(example = "123") @NotNull() @Min(1) Integer numero,
        @Schema(example = "Centro") @NotBlank() String bairro,
        @Schema(example = "Apto 42") @NotBlank() String complemento,
        @Schema(example = "Próximo ao mercado") @NotBlank() String referencia,
        @Schema(description = "Formato #####-###", example = "01001-000") @NotBlank() @Pattern(regexp = "\\d{5}-\\d{3}") String cep,
        @Schema(example = "São Paulo") @NotBlank() String cidade,
        @Schema(example = "SP") @NotBlank() String estado) {}
