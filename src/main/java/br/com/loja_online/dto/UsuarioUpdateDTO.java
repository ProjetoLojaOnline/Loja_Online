package br.com.loja_online.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Builder
public record UsuarioUpdateDTO(
        @NotBlank(message = "O nome não pode ser vazio") String nome,
        @NotBlank(message = "O telefone é obrigatório")
                @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos")
                String telefone,
        String foto,
        String genero) {}
