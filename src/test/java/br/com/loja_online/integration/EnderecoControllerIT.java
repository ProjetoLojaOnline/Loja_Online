package br.com.loja_online.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.repository.EnderecoRepository;

@SuppressWarnings("null")
class EnderecoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private EnderecoRepository enderecoRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        enderecoRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("postCreateDeveRetornar201ComLocationHeader")
    void postCreateDeveRetornar201ComLocationHeader() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();

        mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.logradouro").value(dto.logradouro()))
                .andExpect(jsonPath("$.cep").value(dto.cep()));
    }

    @Test
    @DisplayName("postCreateDeveRetornar401SemToken")
    void postCreateDeveRetornar401SemToken() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();

        mockMvc.perform(post("/endereco/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postCreateDeveRetornar400CepInvalido")
    void postCreateDeveRetornar400CepInvalido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().comCep("CEP-INVALIDO").buildDto();

        mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200ComTokenValido")
    void getByIdDeveRetornar200ComTokenValido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/endereco/{id}", id).header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value(dto.cep()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar401SemToken")
    void getByIdDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/endereco/{id}", 1)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/endereco/{id}", 999999).header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/endereco/{id}", id).header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/endereco/{id}", 1)).andExpect(status().isUnauthorized());
    }
}
