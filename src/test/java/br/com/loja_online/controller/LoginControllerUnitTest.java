package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginRequest;
import br.com.loja_online.exception.ControllerAdviceHandler;
import br.com.loja_online.service.LoginService;
import br.com.loja_online.service.exceptions.AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerUnitTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new ControllerAdviceHandler())
                .build();
    }

    @Test
    @DisplayName("deveRetornar200QuandoLoginComCredenciaisValidas")
    void deveRetornar200QuandoLoginComCredenciaisValidas() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);
        when(loginService.login("user@example.com", "password123")).thenReturn("Login realizado com sucesso");

        // When & Then
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Login realizado com sucesso"));
    }

    @Test
    @DisplayName("deveRetornar401QuandoCredenciaisInvalidas")
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongpassword");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        when(loginService.login("user@example.com", "wrongpassword"))
                .thenThrow(new AuthenticationException("Credenciais inválidas"));

        // When & Then
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar401QuandoEmailNaoExiste")
    void deveRetornar401QuandoEmailNaoExiste() throws Exception {
        LoginRequest loginRequest = new LoginRequest("naoexiste@example.com", "password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        when(loginService.login("naoexiste@example.com", "password123"))
                .thenThrow(new AuthenticationException("Credenciais inválidas"));

        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar401QuandoSenhaIncorreta")
    void deveRetornar401QuandoSenhaIncorreta() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "senhaerrada");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        when(loginService.login("user@example.com", "senhaerrada"))
                .thenThrow(new AuthenticationException("Credenciais inválidas"));

        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar400QuandoRequestBodyVazio")
    void deveRetornar400QuandoRequestBodyVazio() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar400QuandoEmailNulo")
    void deveRetornar400QuandoEmailNulo() throws Exception {
        LoginRequest loginRequest = new LoginRequest(null, "password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar400QuandoSenhaNula")
    void deveRetornar400QuandoSenhaNula() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", null);
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar415QuandoContentTypeInvalido")
    void deveRetornar415QuandoContentTypeInvalido() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(jsonRequest))
                .andExpect(status().isUnsupportedMediaType());
    }
}