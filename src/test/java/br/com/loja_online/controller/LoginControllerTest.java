package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.service.LoginService;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @Test
    @DisplayName("GET /api/logins/user1 deve retornar 200 quando encontrado")
    void buscarPorLogin_ok() throws Exception {
        LoginDTO loginDto = new LoginDTO(1l, "user1", null);
        when(loginService.buscarPorLogin("user1")).thenReturn(loginDto);

        mockMvc.perform(get("/api/logins/user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/logins/inexistente deve retornar 404 quando não encontrado")
    void buscarPorLogin_notFound() throws Exception {
        when(loginService.buscarPorLogin("inexistente"))
                .thenThrow(new ObjectNotFoundException("Login não encontrado: inexistente"));

        mockMvc.perform(get("/api/logins/inexistente").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}