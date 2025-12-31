package br.com.loja_online.controller;

import br.com.loja_online.model.Login;
import br.com.loja_online.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logins")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/{login}")
    public ResponseEntity<Login> buscarPorLogin(@PathVariable String login) {
        Login loginEncontrado = service.buscarPorLogin(login);
        return ResponseEntity.ok(loginEncontrado);
    }
}
