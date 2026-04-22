package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class
UsuarioControllerTest {

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
        @DisplayName("deveCriarUsuarioQuandoPostValido")
        void deveCriarUsuarioQuandoPostValido() throws Exception {
                // Given
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                // When & Then
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.nome").value("João"))
                        .andExpect(jsonPath("$.email").value("joao@example.com"));
        }

        @Test
        @DisplayName("deveRetornar400QuandoPostComCamposObrigatoriosVazios")
        void deveRetornar400QuandoPostComCamposObrigatoriosVazios() throws Exception {
                // Given
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("")
                        .email("")
                        .cpf("")
                        .telefone("")
                        .build();
                LoginDTO loginDTO = new LoginDTO("", "");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                // When & Then
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRetornar400QuandoPostComEmailInvalido")
        void deveRetornar400QuandoPostComEmailInvalido() throws Exception {
                // Given
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("email-invalido")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                // When & Then
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRetornar400QuandoPostComCpfInvalido")
        void deveRetornar400QuandoPostComCpfInvalido() throws Exception {
                // Given
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("abc123")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                // When & Then
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet")
        void deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet() throws Exception {
                // Given: Criar usuário
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                // When & Then: Recuperar
                mockMvc.perform(get("/api/usuarios/{id}", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userId))
                        .andExpect(jsonPath("$.nome").value("João"));
        }

        @Test
        @DisplayName("deveRetornar404QuandoGetPorIdInexistente")
        void deveRetornar404QuandoGetPorIdInexistente() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/usuarios/{id}", 999L))
                        .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deveRetornarUsuarioQuandoGetPorLoginExistente")
        void deveRetornarUsuarioQuandoGetPorLoginExistente() throws Exception {
                // Given: Criar usuário
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
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

                // When & Then
                mockMvc.perform(get("/api/usuarios/login/{login}", "joao"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("João"));
        }

        @Test
        @DisplayName("deveRetornar404QuandoGetPorLoginInexistente")
        void deveRetornar404QuandoGetPorLoginInexistente() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/usuarios/login/{login}", "nonexistent"))
                        .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deveAtualizarUsuarioQuandoPutValido")
        void deveAtualizarUsuarioQuandoPutValido() throws Exception {
                // Given: Criar usuário
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                        .nome("João Atualizado")
                        .telefone("11888888888")
                        .build();

                // When & Then
                mockMvc.perform(put("/api/usuarios/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("João Atualizado"))
                        .andExpect(jsonPath("$.telefone").value("11888888888"));
        }

        @Test
        @DisplayName("deveRetornar404QuandoPutParaIdInexistente")
        void deveRetornar404QuandoPutParaIdInexistente() throws Exception {
                // Given
                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                        .nome("Teste")
                        .telefone("11999999999")
                        .build();

                // When & Then
                mockMvc.perform(put("/api/usuarios/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                        .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deveDeletarUsuarioQuandoDeletePorIdExistente")
        void deveDeletarUsuarioQuandoDeletePorIdExistente() throws Exception {
                // Given: Criar usuário
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                // When & Then
                mockMvc.perform(delete("/api/usuarios/{id}", userId))
                        .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deveRetornar404QuandoDeletePorIdInexistente")
        void deveRetornar404QuandoDeletePorIdInexistente() throws Exception {
                // When & Then
                mockMvc.perform(delete("/api/usuarios/{id}", 999L))
                        .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deveNaoExporSenhaQuandoGetPorLogin")
        void deveNaoExporSenhaQuandoGetPorLogin() throws Exception {
                // Given: Criar usuário
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
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

                // When & Then: Verificar que senha não está no JSON
                mockMvc.perform(get("/api/usuarios/login/{login}", "joao"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.senha").doesNotExist()); // Senha não deve estar presente
        }

        @Test
        @DisplayName("devePermitirAcessoSemAutenticacaoQuandoGetAll")
        void devePermitirAcessoSemAutenticacaoQuandoGetAll() throws Exception {
                // When & Then: Deve funcionar sem auth headers
                mockMvc.perform(get("/api/usuarios"))
                        .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deveRetornarListaVaziaQuandoNenhumUsuario")
        void deveRetornarListaVaziaQuandoNenhumUsuario() throws Exception {
                mockMvc.perform(get("/api/usuarios"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("deveAtualizarNomeQuandoPutNomeValido")
        void deveAtualizarNomeQuandoPutNomeValido() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                        .nome("João Atualizado")
                        .telefone("11999999999")
                        .build();
                mockMvc.perform(put("/api/usuarios/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("João Atualizado"));
        }

        @Test
        @DisplayName("deveRetornar400QuandoPutComNomeVazio")
        void deveRetornar400QuandoPutComNomeVazio() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                        .nome("")
                        .telefone("11999999999")
                        .build();
                mockMvc.perform(put("/api/usuarios/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRetornar415QuandoContentTypeInvalido")
        void deveRetornar415QuandoContentTypeInvalido() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_XML)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("deveAtualizarTelefoneQuandoPutTelefoneValido")
        void deveAtualizarTelefoneQuandoPutTelefoneValido() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                String response = mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
                Long userId = objectMapper.readTree(response).get("id").asLong();

                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                        .nome("João")
                        .telefone("11888888888")
                        .build();
                mockMvc.perform(put("/api/usuarios/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.telefone").value("11888888888"));
        }

        @Test
        @DisplayName("deveRetornar404QuandoDeleteComIdZero")
        void deveRetornar404QuandoDeleteComIdZero() throws Exception {
                mockMvc.perform(delete("/api/usuarios/{id}", 0L))
                        .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deveRetornar409QuandoTentativaDeCriarUsuarioComLoginExistente")
        void deveRetornar409QuandoTentativaDeCriarUsuarioComLoginExistente() throws Exception {
                UsuarioRequestDTO usuario1 = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO login1 = new LoginDTO("joao", "senha123");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuario1, login1))))
                        .andExpect(status().isCreated());

                UsuarioRequestDTO usuario2 = UsuarioRequestDTO.builder()
                        .nome("Maria")
                        .email("maria@example.com")
                        .cpf("98765432100")
                        .telefone("11888888888")
                        .build();
                LoginDTO login2 = new LoginDTO("joao", "senha456");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuario2, login2))))
                        .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("deveRetornar409QuandoTentativaDeCriarUsuarioComEmailJaCadastrado")
        void deveRetornar409QuandoTentativaDeCriarUsuarioComEmailJaCadastrado() throws Exception {
                UsuarioRequestDTO usuario1 = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO login1 = new LoginDTO("joao", "senha123");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuario1, login1))))
                        .andExpect(status().isCreated());

                UsuarioRequestDTO usuario2 = UsuarioRequestDTO.builder()
                        .nome("Maria")
                        .email("joao@example.com")
                        .cpf("98765432100")
                        .telefone("11888888888")
                        .build();
                LoginDTO login2 = new LoginDTO("maria", "senha456");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuario2, login2))))
                        .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("deveRetornar400QuandoSenhaMenorQue6Caracteres")
        void deveRetornar400QuandoSenhaMenorQue6Caracteres() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "abc12");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRetornar400QuandoLoginMenorQue3Caracteres")
        void deveRetornar400QuandoLoginMenorQue3Caracteres() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("ab", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(usuarioDTO, loginDTO);

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deveRetornar405QuandoMetodoDeleteForInvalido")
        void deveRetornar405QuandoMetodoDeleteForInvalido() throws Exception {
                mockMvc.perform(delete("/api/usuarios"))
                        .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("deveRetornar200QuandoLoginComCredenciaisValidas")
        void deveRetornar200QuandoLoginComCredenciaisValidas() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/login/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Login realizado com sucesso"));
        }

        @Test
        @DisplayName("deveRetornar401QuandoLoginComSenhaErrada")
        void deveRetornar401QuandoLoginComSenhaErrada() throws Exception {
                UsuarioRequestDTO usuarioDTO = UsuarioRequestDTO.builder()
                        .nome("João")
                        .email("joao@example.com")
                        .cpf("12345678901")
                        .telefone("11999999999")
                        .build();
                LoginDTO loginDTO = new LoginDTO("joao", "senha123");
                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UsuarioCadastroWrapper(usuarioDTO, loginDTO))))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/login/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"joao@example.com\",\"senha\":\"senhaerrada\"}"))
                        .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deveRetornar401QuandoLoginComUsuarioInvalido")
        void deveRetornar401QuandoLoginComUsuarioInvalido() throws Exception {
                mockMvc.perform(post("/login/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"naoexiste@example.com\",\"senha\":\"senha123\"}"))
                        .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deveRetornar401QuandoLoginComUsuarioESenhaInvalidos")
        void deveRetornar401QuandoLoginComUsuarioESenhaInvalidos() throws Exception {
                mockMvc.perform(post("/login/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"naoexiste@example.com\",\"senha\":\"senhaerrada\"}"))
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
        @DisplayName("deveRetornar400QuandoLoginComSenhaVazia")
        void deveRetornar400QuandoLoginComSenhaVazia() throws Exception {
                mockMvc.perform(post("/login/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"joao@example.com\",\"senha\":\"\"}"))
                        .andExpect(status().isBadRequest());
        }
}
