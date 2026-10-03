package br.com.loja_online.dto.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

import br.com.loja_online.dto.UsuarioRequestDTO;

public class CpfValidator implements ConstraintValidator<ValidCpf, UsuarioRequestDTO> {

    @Override
    public boolean isValid(UsuarioRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        return StringUtils.hasText(dto.getCpf());
    }

    public static boolean isCpfValid(String cpf) {

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

    private static boolean calculateCpf(List<Integer> numerosDoCPF) {

        int primeiraSoma = 0;

        for (int i = 0; i < 9; i++) {
            int peso = 10 - i;
            primeiraSoma += numerosDoCPF.get(i) * peso;
        }

        int primeiroDigitoVerificador = 11 - (primeiraSoma % 11);
        int segundaSoma = 0;

        for (int i = 0; i < 10; i++) {
            int peso = 11 - i;
            segundaSoma += numerosDoCPF.get(i) * peso;
        }

        int segundoDigitoVerificador = 11 - (segundaSoma % 11);

        return numerosDoCPF.get(9) == primeiroDigitoVerificador && numerosDoCPF.get(10) == segundoDigitoVerificador;
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
