package br.com.loja_online.controller;

import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.service.UsuarioService;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("GET /api/usuarios/1 deve retornar 200 quando encontrado")
    void buscarPorId_ok() throws Exception {
        UsuarioDTO usuarioDTO = new UsuarioDTO("Usuario 1", "user1@test.com", "123456789", "1990-01-01", "M",
                "foto.jpg", "ADMIN", null);
        when(usuarioService.findById(1L)).thenReturn(usuarioDTO);

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
        UsuarioDTO usuarioDTO = new UsuarioDTO("Usuario 1", "user1", "123456789", "1990-01-01", "M", "foto.jpg",
                "ADMIN", null);
        when(usuarioService.findByEmail("user1")).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/login/user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/usuarios/login/inexistente deve retornar 404 quando não encontrado")
    void buscarPorLogin_notFound() throws Exception {
        when(usuarioService.findByEmail("inexistente"))
                .thenThrow(new ObjectNotFoundException("Usuário não encontrado com o login: inexistente"));

        mockMvc.perform(get("/api/usuarios/login/inexistente").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}