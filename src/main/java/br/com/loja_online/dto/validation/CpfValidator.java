package br.com.loja_online.dto.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        return validateCpf(cpf);
    }

    public static boolean isCpfValid(String cpf) {
        return validateCpf(cpf);
    }

    private static boolean validateCpf(String cpf) {
        String regexCPFPontuacao = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$";
        String regexCPFSemPontuacao = "^\\d{11}";

        if (!Pattern.matches(regexCPFPontuacao, cpf) && !Pattern.matches(regexCPFSemPontuacao, cpf)) {
            return false;
        }

        List<Integer> numerosDoCPF = cpfToList(cpf);

        if (isSequenceRepeated(numerosDoCPF)) {
            return false;
        }

        return calculateCpf(numerosDoCPF);
    }

    private static List<Integer> cpfToList(String cpf) {
        List<Integer> numerosDoCPF = new ArrayList<>();

        for (int i = 0; i < cpf.length(); i++) {
            char caractere = cpf.charAt(i);

            if (Character.isDigit(caractere)) {
                numerosDoCPF.add(Character.getNumericValue(caractere));
            }
        }
        return numerosDoCPF;
    }

    private static boolean calculateCpf(List<Integer> n) {
        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += n.get(i) * (10 - i);
        }
        int dv1 = digitoVerificador(soma1);

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += n.get(i) * (11 - i);
        }
        int dv2 = digitoVerificador(soma2);

        return n.get(9) == dv1 && n.get(10) == dv2;
    }

    private static int digitoVerificador(int soma) {
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean isSequenceRepeated(List<Integer> numerosDoCPF) {
        int valorAnterior = numerosDoCPF.getFirst();

        for (int i = 1; i < numerosDoCPF.size(); i++) {
            if (numerosDoCPF.get(i) != valorAnterior) {
                return false;
            }
        }

        return true;
    }
}
