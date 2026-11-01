package br.com.loja_online.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(VerificacaoCpfProperties.class)
public class VerificacaoCpfConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Client HTTP para o provedor real de verificação de CPF, já com timeout. O retry com backoff fica no job
     * ({@link br.com.loja_online.service.VerificacaoCpfService}), que reagenda a consulta em vez de segurar a thread.
     */
    @Bean
    public RestClient verificadorCpfRestClient(VerificacaoCpfProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.http().connectTimeout());
        requestFactory.setReadTimeout(properties.http().readTimeout());

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);
        if (StringUtils.hasText(properties.http().baseUrl())) {
            builder.baseUrl(properties.http().baseUrl());
        }
        return builder.build();
    }
}
