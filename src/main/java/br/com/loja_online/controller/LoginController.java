package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/logins")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/{login}")
    public ResponseEntity<LoginDTO> buscarPorLogin(@PathVariable String login) {
        LoginDTO loginEncontrado = service.buscarPorLogin(login);
        return ResponseEntity.ok(loginEncontrado);

    }
}