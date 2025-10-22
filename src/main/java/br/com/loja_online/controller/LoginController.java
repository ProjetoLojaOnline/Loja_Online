package br.com.loja_online.controller;

import br.com.loja_online.domain.login.Login;
import br.com.loja_online.service.LoginService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logins")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping
    public List<Login> listTodos() {
        return service.listaTodos();
    }
}
