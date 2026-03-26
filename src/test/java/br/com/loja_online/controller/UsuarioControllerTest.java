package br.com.loja_online.controller;

// 1. Imports das classes (DTOs e Service)
import br.com.loja_online.dto.*;
import br.com.loja_online.service.UsuarioService;

// 2. Imports do Jackson e Utilidades
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

// 3. Imports do JUnit e Mockito
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// 4. Imports do Spring Test
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

// 5. IMPORTS ESTÁTICOS (para o any, get, post, etc.)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UsuarioService usuarioService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Deve Listar todos os usuários e retorna status 200")
        void deveListarUsuariosComSucesso() throws Exception {
                // GIVEN: Preparando os dados simulados
                UsuarioResponseDTO usuario = UsuarioResponseDTO.builder()
                        .id(1L)
                        .nome("Alfred")
                        .email("alfred@gmail.com")
                        .build();
                List<UsuarioResponseDTO> listaUsuarios = List.of(usuario);

                // Simulando a chamada do service
                Mockito.when(usuarioService.findAll()).thenReturn(listaUsuarios);

                // WHEN & THEN: Executando a requisição e validando
                mockMvc.perform(get("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].nome").value("Alfred"))
                        .andExpect(jsonPath("$.size()").value(1));
        }
        @Test
        @DisplayName("Deve buscar um usuáio por ID e retorna status 200")
        void deveBuscarUsuarioComIdComSucesso() throws Exception {

                Long id = 1l;
                UsuarioResponseDTO usuario = UsuarioResponseDTO.builder()
                        .id(1L)
                        .nome("Alfred")
                        .email("alfred@gmail.com")
                        .build();

                Mockito.when(usuarioService.findById(id)).thenReturn(usuario);

                mockMvc.perform(get("/api/usuarios/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id))
                        .andExpect(jsonPath("$.nome").value("Alfred"))
                        .andExpect(jsonPath("$.email").value("alfred@gmail.com"));
        }
        @Test
        @DisplayName("Deve buscar um usuário por login e retorna status 200")
        void deveBuscarUsuarioPorLoginComSucesso() throws Exception {

                String login = "MazurLogin";
                UsuarioResponseDTO usuario = UsuarioResponseDTO.builder()
                        .id(1l)
                        .nome("Alfred")
                        .email("alfred@gmail.com")
                        .build();

                Mockito.when(usuarioService.findByLogin(login)).thenReturn(usuario);

                mockMvc.perform(get("/api/usuarios/login/{login}",login)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("Alfred"))
                        .andExpect(jsonPath("$.email").value("alfred@gmail.com"));
        }
        @Test
        @DisplayName("Deve cadastrar um usuário com sucesso e retornar 201 Created")
        void deveCadastrarUsuarioComSucesso() throws Exception {
                UsuarioRequestDTO dadosUsuario = UsuarioRequestDTO.builder()
                        .nome("Oliveira")
                        .telefone("44997437334")
                        .email("oliveira@gmail.com")
                        .cpf("12345678901")
                        .build();

                LoginDTO login = LoginDTO.builder()
                        .login("Oliveira_dev")
                        .senha("123456")
                        .build();

                UsuarioCadastroWrapper request = new UsuarioCadastroWrapper(dadosUsuario, login);

                UsuarioResponseDTO repostaService = UsuarioResponseDTO.builder()
                        .id(1L)
                        .nome("Oliveira")
                        .email("oliveira@gmail.com")
                        .build();

                Mockito.when(usuarioService.insert(any(UsuarioRequestDTO.class), any(LoginDTO.class)))
                        .thenReturn(repostaService);

                mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.nome").value("Oliveira"))
                        .andExpect(jsonPath("$.email").value("oliveira@gmail.com"));
        }
        @Test
        @DisplayName("Deve atulizar um usuário com sucesso")

        void deveAtulizarUsuarioComSucesso() throws Exception {
                UsuarioUpdateDTO dadosUsuario = UsuarioUpdateDTO.builder()
                        .nome("Oliveira_Bueno")
                        .telefone("44997837734")
                        .build();

                UsuarioResponseDTO responseDTO = UsuarioResponseDTO.builder()
                        .id(1L)
                        .nome("Oliveira_Bueno")
                        .telefone("44997837734")
                        .build();

                Mockito.when(usuarioService.atualizaUsuario(Mockito.eq(1L), Mockito.any()))
                        .thenReturn(responseDTO);

                mockMvc.perform(MockMvcRequestBuilders.put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosUsuario)))


                        .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nome").value("Oliveira_Bueno"))
                        .andExpect(jsonPath("$.telefone").value("44997837734"));

        }
        @Test
        @DisplayName("Deve deltar usuário com sucesso")
        void deveDeletarUsuarioComSuecsso() throws Exception {

                mockMvc.perform(MockMvcRequestBuilders.delete("/api/usuarios/1"))

                        .andExpect(status().isNoContent());

                Mockito.verify(usuarioService).deleteById(1L);
        }
        @Test
        void deveRetornar404QuandoUsuarioNaoExiste() throws Exception {

                UsuarioUpdateDTO dto = new UsuarioUpdateDTO(
                        "Nome válido",
                        "44999999999",
                        null,
                        null
                );

                Mockito.when(usuarioService.atualizaUsuario(Mockito.eq(1L), Mockito.any()))
                        .thenThrow(new ObjectNotFoundException("Usuario Não encontrado com o ID: 1"));

                mockMvc.perform(MockMvcRequestBuilders.put("/api/usuarios/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))

                        .andExpect(status().isNotFound());
        }
        @Test
        void deveRetorna400QuandoDadoInvalidados() throws Exception {

                UsuarioUpdateDTO dto = new UsuarioUpdateDTO(
                        "",
                                "",
                        null,
                        null
                );

                mockMvc.perform(MockMvcRequestBuilders.put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                        .andExpect(status().isBadRequest());
        }
}