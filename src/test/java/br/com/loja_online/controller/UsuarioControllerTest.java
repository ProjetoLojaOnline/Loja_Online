package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class UsuarioControllerTest extends AbstractIntegrationTest {

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
    @DisplayName("deveCriarUsuarioQuandoPostValido")
    void deveCriarUsuarioQuandoPostValido() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(wrapper.usuario().getNome()))
                .andExpect(jsonPath("$.email").value(wrapper.usuario().getEmail()));
    }

    @Test
    @DisplayName("deveRetornar400QuandoPostComCamposObrigatoriosVazios")
    void deveRetornar400QuandoPostComCamposObrigatoriosVazios() throws Exception {
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(
                UsuarioRequestDTO.builder().nome("").email("").cpf("").telefone("").build(),
                new LoginDTO("", ""));
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar400QuandoPostComEmailInvalido")
    void deveRetornar400QuandoPostComEmailInvalido() throws Exception {
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(
                UsuarioRequestDTO.builder()
                        .nome("Teste").email("email-invalido").cpf("12345678901").telefone("11999999999")
                        .build(),
                new LoginDTO("teste123", "senha123"));
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet")
    void deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @DisplayName("deveRetornar401QuandoGetSemToken")
    void deveRetornar401QuandoGetSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorIdInexistente")
    void deveRetornar404QuandoGetPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveAtualizarUsuarioQuandoPutValido")
    void deveAtualizarUsuarioQuandoPutValido() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Nome Atualizado").telefone("11888888888").build();
        mockMvc.perform(put("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("deveRetornar401QuandoPutSemToken")
    void deveRetornar401QuandoPutSemToken() throws Exception {
        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Teste").telefone("11999999999").build();
        mockMvc.perform(put("/api/usuarios/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoPutParaIdInexistente")
    void deveRetornar404QuandoPutParaIdInexistente() throws Exception {
        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Teste").telefone("11999999999").build();
        mockMvc.perform(put("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveDeletarUsuarioQuandoDeletePorIdExistente")
    void deveDeletarUsuarioQuandoDeletePorIdExistente() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deveRetornar401QuandoDeleteSemToken")
    void deveRetornar401QuandoDeleteSemToken() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoDeletePorIdInexistente")
    void deveRetornar404QuandoDeletePorIdInexistente() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveRetornar409QuandoCriarUsuarioComLoginExistente")
    void deveRetornar409QuandoCriarUsuarioComLoginExistente() throws Exception {
        UsuarioCadastroWrapper w1 = UsuarioBuilder.padrao().comLogin("logindup").buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w1)))
                .andExpect(status().isCreated());

        UsuarioCadastroWrapper w2 = UsuarioBuilder.padrao().comLogin("logindup").buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("deveRetornar200QuandoLoginComCredenciaisValidas")
    void deveRetornar200QuandoLoginComCredenciaisValidas() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar400QuandoLoginComEmailInvalido")
    void deveRetornar400QuandoLoginComEmailInvalido() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"email-invalido\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar415QuandoContentTypeInvalido")
    void deveRetornar415QuandoContentTypeInvalido() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("deveRetornar405QuandoMetodoDeleteForInvalido")
    void deveRetornar405QuandoMetodoDeleteForInvalido() throws Exception {
        mockMvc.perform(delete("/api/usuarios"))
                .andExpect(status().isMethodNotAllowed());
    }
}
