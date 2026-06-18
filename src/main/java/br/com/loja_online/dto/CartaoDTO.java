package br.com.loja_online.dto;

import java.sql.Date;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartaoDTO {

    @Schema(description = "Número do cartão (16 dígitos)", example = "4111111111111111")
    @NotNull(message = "Número do cartão é obrigatório")
    @Positive(message = "Número do cartão deve ser positivo")
    private Long numeroCartao;

    @Schema(description = "Nome impresso no cartão", example = "JOAO A SILVA")
    @NotBlank(message = "Nome no cartão é obrigatório")
    @Size(min = 2, max = 100, message = "Nome no cartão deve ter entre 2 e 100 caracteres")
    private String nomeCartao;

    @Schema(description = "Data de validade futura", example = "2027-12-31")
    @NotNull(message = "Data de validade é obrigatória")
    @Future(message = "Data de validade deve ser uma data futura")
    private Date dataValidade;

    @Schema(description = "CVV (3 ou 4 dígitos)", minimum = "100", maximum = "9999", example = "123")
    @NotNull(message = "CVV é obrigatório")
    @Min(value = 100, message = "CVV deve ter entre 3 e 4 dígitos")
    @Digits(integer = 4, fraction = 0, message = "CVV deve ter entre 3 e 4 dígitos")
    private Integer cvv;

    @Schema(description = "true se este é o cartão padrão", example = "true")
    @NotNull(message = "Indicação de cartão padrão é obrigatória")
    private Boolean defaultCard;
}
