package br.com.loja_online.dto;

import br.com.loja_online.model.enums.StatusVerificacaoCpf;

/** Dados mínimos que o job de verificação de CPF precisa de cada usuário. */
public record VerificacaoCpfPendenteDTO(Long id, String cpf, StatusVerificacaoCpf status, int tentativas) {}
