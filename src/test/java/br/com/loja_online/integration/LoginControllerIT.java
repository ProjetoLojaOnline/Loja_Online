package br.com.loja_online.integration;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.AutenticacaoRequestDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class LoginControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    // ── POST /login/authenticate ──────────────────────────────────────────────

    @Test
    @DisplayName("deveRetornar200QuandoAutenticarPorEmail")
    void deveRetornar200QuandoAutenticarPorEmail() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao();
        criarUsuarioComToken(builder);

        AutenticacaoRequestDTO loginRequest = new AutenticacaoRequestDTO(builder.getEmail(), null, builder.getSenha());
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+")));
    }

    @Test
    @DisplayName("deveRetornar200QuandoAutenticarPorUsername")
    void deveRetornar200QuandoAutenticarPorUsername() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao();
        criarUsuarioComToken(builder);

        AutenticacaoRequestDTO loginRequest = new AutenticacaoRequestDTO(null, builder.getLogin(), builder.getSenha());
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+")));
    }

    @Test
    @DisplayName("deveRetornar401QuandoAutenticarComCredenciaisInexistentes")
    void deveRetornar401QuandoAutenticarComCredenciaisInexistentes() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"naoexiste@example.com\",\"senha\":\"qualquercoisa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar400QuandoNemEmailNemUsernameInformado")
    void deveRetornar400QuandoNemEmailNemUsernameInformado() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("emailOuUsername"))
                .andExpect(jsonPath("$.errors[0].message").value("Informe seu e-mail ou username"));
    }

    @Test
    @DisplayName("deveRetornar400QuandoEmailComFormatoInvalido")
    void deveRetornar400QuandoEmailComFormatoInvalido() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("Formato de e-mail inválido"));
    }

    @Test
    @DisplayName("deveRetornar400QuandoSenhaNula")
    void deveRetornar400QuandoSenhaNula() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"senha\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("senha"));
    }

    @Test
    @DisplayName("deveRetornar415QuandoContentTypeInvalido")
    void deveRetornar415QuandoContentTypeInvalido() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("{\"email\":\"user@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ── GET /login/buscar/{login} ─────────────────────────────────────────────

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoGetPorLoginExistente")
    void deveRetornarLoginDTOQuandoGetPorLoginExistente() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao().comLogin("logintest1");
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "logintest1").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("logintest1"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginInexistente")
    void deveRetornar404QuandoGetPorLoginInexistente() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "naoexiste").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveNaoExporSenhaQuandoGetPorLogin")
    void deveNaoExporSenhaQuandoGetPorLogin() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao().comLogin("logintest2");
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(builder.buildWrapper())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "logintest2").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar401QuandoGetPorLoginSemToken")
    void deveRetornar401QuandoGetPorLoginSemToken() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "qualquer")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginComParametroVazio")
    void deveRetornar404QuandoGetPorLoginComParametroVazio() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveManterIntegridadeQuandoBuscarLoginAposCriacao")
    void deveManterIntegridadeQuandoBuscarLoginAposCriacao() throws Exception {
        UsuarioBuilder primeiroBuilder = UsuarioBuilder.padrao().comLogin("integlogin1");
        UsuarioBuilder segundoBuilder = UsuarioBuilder.padrao().comLogin("integlogin2");
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primeiroBuilder.buildWrapper())))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segundoBuilder.buildWrapper())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "integlogin1").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin1"));
        mockMvc.perform(get("/login/buscar/{login}", "integlogin2").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin2"));
    }
}
