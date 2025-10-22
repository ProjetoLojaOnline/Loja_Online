package br.com.loja_online.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EnderecoDTO(
     @NotBlank
    String logradouro,
    @Min(1)
    Integer numero,
     @NotBlank
    String bairro,
     @NotBlank
    String complemento,
     @NotBlank
     String referencia,
     @NotBlank
    String cep,
     @NotBlank
    String cidade,
     @NotBlank
    String estado
) {
}
