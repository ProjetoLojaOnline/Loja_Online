package br.com.loja_online.unit.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import br.com.loja_online.exception.ControllerAdviceHandler;
import br.com.loja_online.exception.StandardError;

class ControllerAdviceHandlerTest {

    private final ControllerAdviceHandler handler = new ControllerAdviceHandler();

    @ParameterizedTest(name = "{0}")
    @CsvSource(
            delimiter = '|',
            value = {
                "Detail: Key (cpf)=(52998224725) already exists.        | Este CPF já está cadastrado!",
                "Detalhe: Chave (email)=(a@b.com) já existe.            | Este e-mail já está cadastrado!",
                "Detail: Key (telefone)=(11999999999) already exists.   | Esse telefone já está em uso!",
                "Detail: Key (login)=(joao) already exists.             | Este login já está em uso!",
                "null value in column \"nome\" violates not-null        | Os dados informados conflitam com um registro já existente.",
            })
    void deveRetornar409ComMensagemDoCampoSemExporOValor(String erroDoBanco, String mensagemEsperada) {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("could not execute statement", new SQLException(erroDoBanco));

        ResponseEntity<StandardError> resposta =
                handler.dataIntegrityViolationException(ex, new MockHttpServletRequest("POST", "/api/usuarios"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().getMessage()).isEqualTo(mensagemEsperada);
        assertThat(resposta.getBody().getPath()).isEqualTo("/api/usuarios");
    }
}
