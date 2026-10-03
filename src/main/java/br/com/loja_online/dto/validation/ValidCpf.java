package br.com.loja_online.dto.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = CpfValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {
    String message() default "Informe seu CPF";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
