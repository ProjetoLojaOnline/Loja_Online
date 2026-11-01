package br.com.loja_online.dto;

import jakarta.validation.constraints.NotBlank;

import br.com.loja_online.dto.validation.ValidCpf;

public record CpfCorrecaoDTO(@NotBlank(message = "O CPF é obrigatório") @ValidCpf String cpf) {}
