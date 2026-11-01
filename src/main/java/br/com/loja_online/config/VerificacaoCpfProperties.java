package br.com.loja_online.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configurações da verificação assíncrona de CPF (prefixo {@code verificacao.cpf}). */
@ConfigurationProperties(prefix = "verificacao.cpf")
public record VerificacaoCpfProperties(
        @DefaultValue("fake") String provedor,
        @DefaultValue Job job,
        @DefaultValue Http http,
        @DefaultValue Fake fake) {

    /**
     * @param lote quantos usuários o job processa por execução
     * @param backoffInicial espera após a 1ª falha do provedor; dobra a cada nova falha
     * @param backoffMaximo teto da espera entre tentativas (o registro nunca é descartado)
     * @param reverificarRejeitadosApos CPFs rejeitados são consultados de novo depois desse prazo
     */
    public record Job(
            @DefaultValue("50") int lote,
            @DefaultValue("1m") Duration backoffInicial,
            @DefaultValue("1h") Duration backoffMaximo,
            @DefaultValue("7d") Duration reverificarRejeitadosApos) {}

    /** Timeouts do client HTTP do provedor real. */
    public record Http(
            String baseUrl, @DefaultValue("5s") Duration connectTimeout, @DefaultValue("60s") Duration readTimeout) {}

    /**
     * @param rejeitados CPFs (só dígitos) que o fake responde como inexistentes
     * @param falhas CPFs (só dígitos) para os quais o fake simula falha do provedor
     */
    public record Fake(@DefaultValue List<String> rejeitados, @DefaultValue List<String> falhas) {}
}
