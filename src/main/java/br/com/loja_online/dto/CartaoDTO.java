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
    private Long numeroCartao;
    
    @NotBlank
    private String nomeCartao;
    
    @NotNull
    private Date dataValidade;
    
    @NotNull
    private Integer cvv;
}
