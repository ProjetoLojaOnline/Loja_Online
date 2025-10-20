package br.com.loja_online.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EnderecoDTO(
     @NotBlank(message = "O logradouro não pode estar vazio.")
    String logradouro,
    @NotNull(message = "O número não pode ser nulo.")
    @Min(1)
    Integer numero,
     @NotBlank(message = "O bairro não pode estar vazio.")
    String bairro,
     @NotBlank(message = "O complemento não pode estar vazio.")
    String complemento,
     @NotBlank(message = "A referência não pode estar vazia.")
     String referencia,
     @NotBlank(message = "O CEP não pode estar vazio.")
     @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 12345-678.")
    String cep,
     @NotBlank(message = "A cidade não pode estar vazia.")
    String cidade,
     @NotBlank(message = "O estado não pode estar vazio.")
    String estado
) {
}
