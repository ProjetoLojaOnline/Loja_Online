package br.com.loja_online.service;

import br.com.loja_online.model.Login;
import br.com.loja_online.model.enums.Role;
import br.com.loja_online.repository.LoginRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AutenticacaoServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        loginRepository.deleteAll();

        Login login = Login.builder()
                .login("teste@email.com")
                .senha(passwordEncoder.encode("123456"))
                .role(Role.ROLE_USER)
                .build();

        loginRepository.save(login);
    }

    @Test
    @DisplayName("deveRetornar200ETokenQuandoCredenciaisValidas")
    void deveRetornar200ETokenQuandoCredenciaisValidas() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "teste@email.com",
                            "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("deveRetornar401QuandoSenhaErrada")
    void deveRetornar401QuandoSenhaErrada() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "teste@email.com",
                            "senha": "senhaerrada"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("deveRetornar401QuandoUsuarioNaoExiste")
    void deveRetornar401QuandoUsuarioNaoExiste() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "naoexiste@email.com",
                            "senha": "123456"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("deveRetornar401QuandoAcessarRotaProtegidaSemToken")
    void deveRetornar401QuandoAcessarRotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }
}