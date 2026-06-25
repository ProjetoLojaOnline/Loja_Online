package br.com.loja_online.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import br.com.loja_online.dto.validation.ValidLoginIdentificador;

@ValidLoginIdentificador
public record AutenticacaoRequestDTO(
        @Email(message = "Formato de e-mail inválido") String email,
        String username,
        @NotBlank(message = "Senha é obrigatória") String senha) {}
