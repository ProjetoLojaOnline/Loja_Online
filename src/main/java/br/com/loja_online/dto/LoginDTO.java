package br.com.loja_online.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginDTO(
        @NotBlank(message = "Login é obrigatório")
        @Size(min = 3, message = "Login deve ter no mínimo 3 caracteres")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
        String senha
) {
}
