package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
