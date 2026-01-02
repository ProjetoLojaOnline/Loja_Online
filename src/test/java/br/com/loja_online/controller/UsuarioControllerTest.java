package br.com.loja_online.controller;

import br.com.loja_online.model.Usuario;
import br.com.loja_online.service.UsuarioService;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("GET /api/usuarios/1 deve retornar 200 quando encontrado")
    void buscarPorId_ok() throws Exception {
        Usuario usuario = Usuario.builder().id(1L).login("user1").nome("Usuario 1").build();
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/usuarios/999 deve retornar 404 quando não encontrado")
    void buscarPorId_notFound() throws Exception {
        when(usuarioService.findById(999L)).thenThrow(new ObjectNotFoundException("Usuário não encontrado com o ID: 999"));

        mockMvc.perform(get("/api/usuarios/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/usuarios/login/user1 deve retornar 200 quando encontrado")
    void buscarPorLogin_ok() throws Exception {
        Usuario usuario = Usuario.builder().id(1L).login("user1").nome("Usuario 1").build();
        when(usuarioService.findByLogin("user1")).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/login/user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/usuarios/login/inexistente deve retornar 404 quando não encontrado")
    void buscarPorLogin_notFound() throws Exception {
        when(usuarioService.findByLogin("inexistente")).thenThrow(new ObjectNotFoundException("Usuário não encontrado com o login: inexistente"));

        mockMvc.perform(get("/api/usuarios/login/inexistente").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}