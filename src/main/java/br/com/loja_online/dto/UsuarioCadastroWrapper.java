package br.com.loja_online.dto;

import jakarta.validation.Valid;

public record UsuarioCadastroWrapper(@Valid UsuarioRequestDTO usuario, @Valid LoginDTO login) {}
