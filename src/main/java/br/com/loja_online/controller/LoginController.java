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
    
        @PostMapping
        public ResponseEntity<Void> criar(@Valid @RequestBody LoginDTO loginDTO) {
            loginDTO = service.salvar(loginDTO);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(loginDTO.id())
                    .toUri();
            return ResponseEntity.status(HttpStatus.CREATED).build();

        }
}