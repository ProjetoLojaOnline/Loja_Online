package br.com.loja_online.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

import br.com.loja_online.dto.AutenticacaoRequestDTO;

public class LoginIdentificadorValidator
        implements ConstraintValidator<ValidLoginIdentificador, AutenticacaoRequestDTO> {

    @Override
    public boolean isValid(AutenticacaoRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        return StringUtils.hasText(dto.email()) || StringUtils.hasText(dto.username());
    }
}
