package br.com.loja_online.dto;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequestDTO(
        @NotBlank(message = "Identificador é obrigatório") String identificador,
        @NotBlank(message = "Senha é obrigatória") String senha) {}
