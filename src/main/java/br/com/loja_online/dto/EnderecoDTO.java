package br.com.loja_online.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EnderecoDTO(
     @NotBlank()
    String logradouro,
    @NotNull()
    @Min(1)
    Integer numero,
     @NotBlank()
    String bairro,
     @NotBlank()
    String complemento,
     @NotBlank()
     String referencia,
     @NotBlank()
     @Pattern(regexp = "\\d{5}-\\d{3}" )
    String cep,
     @NotBlank()
    String cidade,
     @NotBlank()
    String estado
) {
}
