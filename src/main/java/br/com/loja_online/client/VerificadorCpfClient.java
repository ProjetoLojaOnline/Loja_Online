package br.com.loja_online.client;

/**
 * Provedor externo que diz se um CPF existe de verdade (não só se o dígito verificador é válido).
 *
 * <p>Para trocar de provedor, crie outra implementação anotada com
 * {@code @ConditionalOnProperty(name = "verificacao.cpf.provedor", havingValue = "<nome>")} e mude a property. O job
 * depende só desta interface.
 */
public interface VerificadorCpfClient {

    /**
     * @param cpf somente dígitos
     * @return {@code true} se o CPF existe, {@code false} se não existe
     * @throws RuntimeException se o provedor falhar (timeout, 5xx...); o job reagenda com backoff
     */
    boolean cpfExiste(String cpf);
}
