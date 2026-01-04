package br.com.loja_online.dto;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartaoDTO {
    @NotNull
    @NotBlank
    private Long numeroCartao;
    
    @NotNull
    @NotBlank
    private String nomeCartao;
    
    @NotNull
    @NotBlank
    private Date dataValidade;
    
    @NotNull
    @NotBlank
    private Integer cvv;

    @NotNull
    @NotBlank
    private Boolean defaultCard;
}
