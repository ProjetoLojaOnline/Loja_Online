package br.com.loja_online.security;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TokenServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Login loginSalvo;

    @BeforeEach
    void setUp() {
        loginRepository.deleteAll();

        loginSalvo = Login.builder()
                .login("testuser@email.com")
                .senha(passwordEncoder.encode("123456"))
                .role(Role.ROLE_USER)
                .build();

        loginRepository.save(loginSalvo);
    }

    @Test
    @DisplayName("deveRetornarTokenAoFazerLoginComCredenciaisValidas")
    void deveRetornarTokenAoFazerLoginComCredenciaisValidas() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "testuser@email.com",
                                  "senha": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("deveRetornar401AoFazerLoginComSenhaErrada")
    void deveRetornar401AoFazerLoginComSenhaErrada() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "testuser@email.com",
                                  "senha": "senhaerrada"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar401AoFazerLoginComUsuarioInexistente")
    void deveRetornar401AoFazerLoginComUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "naoexiste@email.com",
                                  "senha": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveAcessarRotaProtegidaComTokenValido")
    void deveAcessarRotaProtegidaComTokenValido() throws Exception {
        String token = tokenService.gerarToken(loginSalvo);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deveRetornar401AoAcessarRotaProtegidaSemToken")
    void deveRetornar401AoAcessarRotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar401AoAcessarRotaProtegidaComTokenInvalido")
    void deveRetornar401AoAcessarRotaProtegidaComTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("devePermitirCadastroSemToken")
    void devePermitirCadastroSemToken() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": {
                                    "nome": "Novo Usuario",
                                    "telefone": "11999999999",
                                    "email": "novo@email.com",
                                    "cpf": "12345678901",
                                    "dataNascimento": "1990-01-01",
                                    "genero": "Masculino",
                                    "tipo": "Cliente"
                                  },
                                  "login": {
                                    "login": "novo@email.com",
                                    "senha": "123456"
                                  }
                                }
                                """))
                .andExpect(status().isCreated());
    }
}