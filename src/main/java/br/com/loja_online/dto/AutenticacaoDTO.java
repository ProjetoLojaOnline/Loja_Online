package br.com.loja_online.dto;

import br.com.loja_online.model.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutenticacaoDTO(
        Integer id,
        String nome,
        @NotBlank(message = "O campo senha é obrigatório.")
        @Size(min = 8, message = "A senha deve conter no mínimo 8 caracteres.")
        String senha,
        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O formato de email é inválido.")
        String email,
        String telefone,
        String dataNascimento,
        String genero,
        String foto,
        String tipo) {
}
