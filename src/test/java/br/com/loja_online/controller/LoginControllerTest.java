package br.com.loja_online.controller;

import br.com.loja_online.model.Login;
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

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @Test
    @DisplayName("GET /logins/user1 deve retornar 200 quando encontrado")
    void buscarPorLogin_ok() throws Exception {
        Login login = new Login("user1", "senha");
        when(loginService.buscarPorLogin("user1")).thenReturn(login);

        mockMvc.perform(get("/logins/user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /logins/inexistente deve retornar 404 quando não encontrado")
    void buscarPorLogin_notFound() throws Exception {
        when(loginService.buscarPorLogin("inexistente"))
                .thenThrow(new ObjectNotFoundException("Login não encontrado: inexistente"));

        mockMvc.perform(get("/logins/inexistente").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}