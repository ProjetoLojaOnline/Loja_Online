package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.LoginRequest;
import br.com.loja_online.model.Login;
import br.com.loja_online.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/api/logins/{login}")
    public ResponseEntity<LoginDTO> buscarPorLogin(@PathVariable String login) {
        LoginDTO loginEncontrado = service.buscarPorLogin(login);
        return ResponseEntity.ok(loginEncontrado);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String mensagem = service.login(loginRequest.getEmail(), loginRequest.getSenha());
            return ResponseEntity.ok(mensagem);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}