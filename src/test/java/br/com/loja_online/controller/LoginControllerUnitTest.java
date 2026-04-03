package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginRequest;
import br.com.loja_online.service.LoginService;
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

@ExtendWith(MockitoExtension.class) //
class LoginControllerUnitTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper(); // Instancia manualmente

    @Mock
    private LoginService loginService; // Uso @Mock em vez de @MockBean

    @InjectMocks
    private LoginController loginController; // Injeta o mock automaticamente no construtor

    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    @DisplayName("deveRetornar200QuandoLoginComCredenciaisValidas")
    void deveRetornar200QuandoLoginComCredenciaisValidas() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);
        when(loginService.login("user@example.com", "password123")).thenReturn("Login realizado com sucesso");

        // When & Then
        mockMvc.perform(post("/login")
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

        // Aqui simulamos a falha
        when(loginService.login("user@example.com", "wrongpassword"))
                .thenThrow(new RuntimeException("Credenciais inválidas"));

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                .andExpect(status().isUnauthorized());
    }
}