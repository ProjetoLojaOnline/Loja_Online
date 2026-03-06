package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import br.com.loja_online.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        @DisplayName("POST /api/usuarios deve retornar 201 quando o usuário é cadastrado com sucesso")
        void criar_created() throws Exception {
                UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                                .nome("novoUser")
                                .telefone("123456789")
                                .email("novoUser@email.com")
                                .cpf("123.456.789-00")
                                .dataNascimento("1990-01-01")
                                .genero("M")
                                .foto("foto.jpg")
                                .tipo("CLIENTE")
                                .cartoes(Collections.emptyList())
                                .enderecos(Collections.emptyList())
                                .build();

                UsuarioResponseDTO response = UsuarioResponseDTO.builder()
                                .id(1L)
                                .nome("novoUser")
                                .telefone("123456789")
                                .email("novoUser@email.com")
                                .cpf("123.456.789-00")
                                .dataNascimento("1990-01-01")
                                .genero("M")
                                .foto("foto.jpg")
                                .tipo("CLIENTE")
                                .cartoes(Collections.emptyList())
                                .enderecos(Collections.emptyList())
                                .build();

                LoginDTO loginDto = new LoginDTO(null, "novoUser", "senha123");
                UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(request, loginDto);

                when(usuarioService.insert(any(UsuarioRequestDTO.class), any(LoginDTO.class))).thenReturn(response);

                mockMvc.perform(post("/api/usuarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrapper)))
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("GET /api/usuarios/1 deve retornar 200 quando encontrado")
        void buscarPorId_ok() throws Exception {
                UsuarioResponseDTO response = UsuarioResponseDTO.builder()
                                .id(1L)
                                .nome("user")
                                .telefone("123456789")
                                .email("Alfred@gmail.com")
                                .cpf("123.456.789-00")
                                .dataNascimento("01/01/2000")
                                .genero("M")
                                .foto("foto.jpg")
                                .tipo("CLIENTE")
                                .cartoes(Collections.emptyList())
                                .enderecos(Collections.emptyList())
                                .build();

                when(usuarioService.findById(1L)).thenReturn(response);

                mockMvc.perform(get("/api/usuarios/1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/usuarios/999 deve retornar 404 quando não encontrado")
        void buscarPorId_notFound() throws Exception {
                when(usuarioService.findById(999L))
                                .thenThrow(new ObjectNotFoundException("Usuário não encontrado com o ID: 999"));

                mockMvc.perform(get("/api/usuarios/999").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/usuarios/login/user1 deve retornar 200 quando encontrado")
        void buscarPorLogin_ok() throws Exception {
                UsuarioResponseDTO response = UsuarioResponseDTO.builder()
                                .id(1L)
                                .nome("user1")
                                .telefone("123456789")
                                .email("user1@email.com")
                                .cpf("123.456.789-00")
                                .dataNascimento("1990-01-01")
                                .genero("M")
                                .foto("foto.jpg")
                                .tipo("ADMIN")
                                .cartoes(Collections.emptyList())
                                .enderecos(Collections.emptyList())
                                .build();

                when(usuarioService.findByLogin("user1")).thenReturn(response);

                mockMvc.perform(get("/api/usuarios/login/user1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/usuarios/login/inexistente deve retornar 404 quando não encontrado")
        void buscarPorLogin_notFound() throws Exception {
                when(usuarioService.findByLogin("inexistente"))
                                .thenThrow(new ObjectNotFoundException(
                                                "Usuário não encontrado com o login: inexistente"));

                mockMvc.perform(get("/api/usuarios/login/inexistente").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }
}