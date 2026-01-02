package br.com.loja_online.dto;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CartaoDTO {
    @NotBlank()
    @NotEmpty()
    @NotNull()
    private Long numeroCartao;
    @NotBlank()
    @NotEmpty()
    @NotNull()
    private String nomeCartao;
    @NotBlank()
    @NotEmpty()
    @NotNull()
    private Date dataValidade;
    @NotBlank()
    @NotEmpty()
    @NotNull()
    private Integer cvv;
}
