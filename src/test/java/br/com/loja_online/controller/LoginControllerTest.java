package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class LoginControllerTest extends AbstractIntegrationTest {

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

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoGetPorLoginExistente")
    void deveRetornarLoginDTOQuandoGetPorLoginExistente() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao().comLogin("logintest1");
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "logintest1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("logintest1"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginInexistente")
    void deveRetornar404QuandoGetPorLoginInexistente() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "naoexiste")
                        .header("Authorization", "Bearer " + authToken))
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

        mockMvc.perform(get("/login/buscar/{login}", "logintest2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar401QuandoGetPorLoginSemToken")
    void deveRetornar401QuandoGetPorLoginSemToken() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginComParametroVazio")
    void deveRetornar404QuandoGetPorLoginComParametroVazio() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveManterIntegridadeQuandoBuscarLoginAposCriacao")
    void deveManterIntegridadeQuandoBuscarLoginAposCriacao() throws Exception {
        UsuarioBuilder b1 = UsuarioBuilder.padrao().comLogin("integlogin1");
        UsuarioBuilder b2 = UsuarioBuilder.padrao().comLogin("integlogin2");
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b1.buildWrapper())))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b2.buildWrapper())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "integlogin1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin1"));
        mockMvc.perform(get("/login/buscar/{login}", "integlogin2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin2"));
    }
}
