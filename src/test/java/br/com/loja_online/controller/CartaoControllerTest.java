package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.repository.CartaoRepository;

@SuppressWarnings("null")
class CartaoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        cartaoRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("postCreateDeveRetornar201ComTokenValido")
    void postCreateDeveRetornar201ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCartao").value(dto.getNomeCartao()))
                .andExpect(jsonPath("$.cvv").value(dto.getCvv()));
    }

    @Test
    @DisplayName("postCreateDeveRetornar401SemToken")
    void postCreateDeveRetornar401SemToken() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        mockMvc.perform(post("/cartao/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postCreateDeveRetornar400CvvInvalido")
    void postCreateDeveRetornar400CvvInvalido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().comCvv(50).buildDto();

        mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200ComTokenValido")
    void getByIdDeveRetornar200ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCartao").value(dto.getNomeCartao()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar401SemToken")
    void getByIdDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/cartao/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/cartao/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("patchDeveRetornar200ComTokenValido")
    void patchDeveRetornar200ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        CartaoDTO atualizado = CartaoBuilder.padrao().comNomeCartao("Nome Atualizado").buildDto();
        mockMvc.perform(patch("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCartao").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/cartao/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
