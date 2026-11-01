package br.com.loja_online.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Dispara a verificação de CPF periodicamente. Desligue com {@code verificacao.cpf.job.enabled=false}. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "verificacao.cpf.job.enabled", havingValue = "true", matchIfMissing = true)
public class VerificacaoCpfJob {

    private final VerificacaoCpfService verificacaoCpfService;

    @Scheduled(
            fixedDelayString = "${verificacao.cpf.job.intervalo:PT30S}",
            initialDelayString = "${verificacao.cpf.job.atraso-inicial:PT10S}")
    public void executar() {
        verificacaoCpfService.processarPendentes();
    }
}
