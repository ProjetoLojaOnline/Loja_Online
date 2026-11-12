package br.com.loja_online.dto.validation;

import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida o CNPJ pelos dígitos verificadores. Aceita o formato numérico e o alfanumérico (em vigor desde jul/2026), em
 * que as 12 primeiras posições podem ter letras e as 2 últimas continuam sendo dígitos.
 */
public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {

    private static final Pattern COM_PONTUACAO =
            Pattern.compile("^[0-9A-Z]{2}\\.[0-9A-Z]{3}\\.[0-9A-Z]{3}/[0-9A-Z]{4}-\\d{2}$");
    private static final Pattern SEM_PONTUACAO = Pattern.compile("^[0-9A-Z]{12}\\d{2}$");
    private static final Pattern DIGITOS_REPETIDOS = Pattern.compile("^(\\d)\\1{13}$");

    private static final int[] PESOS_PRIMEIRO_DV = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DV = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        // null é responsabilidade do @NotBlank (convenção do Bean Validation)
        if (cnpj == null) {
            return true;
        }
        return isCnpjValid(cnpj);
    }

    public static boolean isCnpjValid(String cnpj) {
        if (cnpj == null) {
            return false;
        }
        String valor = cnpj.toUpperCase(Locale.ROOT);
        if (!COM_PONTUACAO.matcher(valor).matches()
                && !SEM_PONTUACAO.matcher(valor).matches()) {
            return false;
        }

        String c = normalizar(valor);
        if (DIGITOS_REPETIDOS.matcher(c).matches()) {
            return false;
        }

        int dv1 = digitoVerificador(c, PESOS_PRIMEIRO_DV);
        int dv2 = digitoVerificador(c, PESOS_SEGUNDO_DV);
        return dv1 == c.charAt(12) - '0' && dv2 == c.charAt(13) - '0';
    }

    /** Remove a pontuação e deixa as letras em maiúsculo: "12.abc.345/01de-35" vira "12ABC34501DE35". */
    public static String normalizar(String cnpj) {
        return cnpj == null ? null : cnpj.toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Z]", "");
    }

    // Cada caractere vale (código ASCII - 48): '0'..'9' = 0..9 e 'A'..'Z' = 17..42
    private static int digitoVerificador(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
