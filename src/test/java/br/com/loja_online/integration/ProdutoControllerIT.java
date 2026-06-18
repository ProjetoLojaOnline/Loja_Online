package br.com.loja_online.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.ProdutoRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class ProdutoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        produtoRepository.deleteAll();
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
        authToken = criarVendedorEObterToken();
    }

    @Test
    @DisplayName("getDeveRetornar200SemToken")
    void getDeveRetornar200SemToken() throws Exception {
        mockMvc.perform(get("/produto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200SemToken")
    void getByIdDeveRetornar200SemToken() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/produto/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(dto.nome()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/produto/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("postDeveRetornar201ComTokenValido")
    void postDeveRetornar201ComTokenValido() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();

        mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(dto.nome()))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("postDeveRetornar401SemToken")
    void postDeveRetornar401SemToken() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();

        mockMvc.perform(post("/produto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postDeveRetornar403ComTokenRoleUser")
    void postDeveRetornar403ComTokenRoleUser() throws Exception {
        String userToken = criarUsuarioEObterToken();
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();

        mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("postDeveRetornar400ComNomeVazio")
    void postDeveRetornar400ComNomeVazio() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().comNome("").buildDto();

        mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/produto/{id}", id).header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/produto/{id}", 1)).andExpect(status().isUnauthorized());
    }
}
