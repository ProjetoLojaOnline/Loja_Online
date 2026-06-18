package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    @BeforeEach
    void setUp() {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoGetPorLoginExistente")
    void deveRetornarLoginDTOQuandoGetPorLoginExistente() throws Exception {
        // Given: Criar usuário e login via POST
        UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                .nome("João Silva")
                .email("joao@example.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .build();
        LoginDTO loginDTO = new LoginDTO("joao", "senha123");
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        // When & Then: Buscar por login
        mockMvc.perform(get("/login/buscar/{login}", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("joao"))
                .andExpect(jsonPath("$.senha").doesNotExist()); // Senha não exposta
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginInexistente")
    void deveRetornar404QuandoGetPorLoginInexistente() throws Exception {
        // Given: Nenhum login criado

        // When & Then: Buscar por login inexistente
        mockMvc.perform(get("/login/buscar/{login}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveNaoExporSenhaQuandoGetPorLogin")
    void deveNaoExporSenhaQuandoGetPorLogin() throws Exception {
        // Given: Criar usuário e login
        UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                .nome("Maria Oliveira")
                .email("maria@example.com")
                .cpf("98765432100")
                .telefone("11888888888")
                .build();
        LoginDTO loginDTO = new LoginDTO("maria", "senha456");
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        // When & Then: Verificar que senha não está no JSON
        mockMvc.perform(get("/login/buscar/{login}", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("maria"))
                .andExpect(jsonPath("$.senha").doesNotExist()); // Segurança: senha não exposta
    }

    @Test
    @DisplayName("devePermitirAcessoSemAutenticacaoQuandoGetPorLogin")
    void devePermitirAcessoSemAutenticacaoQuandoGetPorLogin() throws Exception {
        // Given: Criar usuário e login
        UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                .nome("Carlos Souza")
                .email("carlos@example.com")
                .cpf("11122233344")
                .telefone("11777777777")
                .build();
        LoginDTO loginDTO = new LoginDTO("carlos", "senha789");
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        // When & Then: Deve funcionar sem auth headers (permitAll)
        mockMvc.perform(get("/login/buscar/{login}", "carlos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginComParametroVazio")
    void deveRetornar404QuandoGetPorLoginComParametroVazio() throws Exception {
        // When & Then
        mockMvc.perform(get("/login/buscar/{login}", ""))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginComParametroNulo")
    void deveRetornar404QuandoGetPorLoginComParametroNulo() throws Exception {
        // When & Then: Path variable não pode ser nula, mas simular inexistente
        mockMvc.perform(get("/login/buscar/{login}", "null"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveManterIntegridadeQuandoBuscarLoginAposCriacao")
    void deveManterIntegridadeQuandoBuscarLoginAposCriacao() throws Exception {
        // Given: Criar múltiplos usuários
        UsuarioRequestDTO usuario1 = UsuarioRequestDTO.builder()
                .nome("João")
                .email("joao@example.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .build();
        LoginDTO login1 = new LoginDTO("joao", "senha123");
        UsuarioCadastroWrapper wrapper1 = new UsuarioCadastroWrapper(usuario1, login1);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper1)))
                .andExpect(status().isCreated());

        UsuarioRequestDTO usuario2 = UsuarioRequestDTO.builder()
                .nome("Maria")
                .email("maria@example.com")
                .cpf("98765432100")
                .telefone("11888888888")
                .build();
        LoginDTO login2 = new LoginDTO("maria", "senha456");
        UsuarioCadastroWrapper wrapper2 = new UsuarioCadastroWrapper(usuario2, login2);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper2)))
                .andExpect(status().isCreated());

        // When & Then: Buscar cada um corretamente
        mockMvc.perform(get("/login/buscar/{login}", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("joao"));

        mockMvc.perform(get("/login/buscar/{login}", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("maria"));
    }
}
