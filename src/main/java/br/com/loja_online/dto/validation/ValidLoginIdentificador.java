package br.com.loja_online.dto.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = LoginIdentificadorValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLoginIdentificador {
    String message() default "Informe seu e-mail ou username";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
