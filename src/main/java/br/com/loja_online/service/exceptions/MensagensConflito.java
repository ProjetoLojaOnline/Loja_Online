package br.com.loja_online.service.exceptions;

/**
 * Mensagens de 409 para dados únicos. Usadas tanto na checagem prévia do serviço quanto no handler de
 * {@code DataIntegrityViolationException}, que pega o caso de dois cadastros simultâneos passarem pela checagem.
 */
public final class MensagensConflito {

    public static final String LOGIN = "Este login já está em uso!";
    public static final String EMAIL = "Este e-mail já está cadastrado!";
    public static final String CPF = "Este CPF já está cadastrado!";
    public static final String TELEFONE = "Esse telefone já está em uso!";
    public static final String GENERICA = "Os dados informados conflitam com um registro já existente.";

    private MensagensConflito() {}
}
