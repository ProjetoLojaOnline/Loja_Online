package br.com.loja_online.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.service.LoginService;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/buscar/{login}")
    public ResponseEntity<LoginDTO> buscarPorLogin(@PathVariable String login) {
        LoginDTO loginEncontrado = service.buscarPorLogin(login);
        return ResponseEntity.ok(loginEncontrado);
    }
}
