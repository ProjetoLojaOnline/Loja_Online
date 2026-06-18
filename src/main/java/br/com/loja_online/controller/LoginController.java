package br.com.loja_online.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.loja_online.dto.AutenticacaoRequestDTO;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.service.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação", description = "Login e consulta de credenciais")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/buscar/{login}")
    @Operation(summary = "Buscar login por username")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Login não encontrado")
    })
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<LoginDTO> buscarPorLogin(@PathVariable String login) {
        return ResponseEntity.ok(service.buscarPorLogin(login));
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Autenticar usuário", description = "Retorna JWT Bearer token. Rota pública.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token JWT gerado"),
        @ApiResponse(responseCode = "400", description = "Email ou senha inválidos (formato)"),
        @ApiResponse(responseCode = "401", description = "Credenciais incorretas")
    })
    public ResponseEntity<String> login(@Valid @RequestBody AutenticacaoRequestDTO loginRequest) {
        return ResponseEntity.ok(service.login(loginRequest.identificador(), loginRequest.senha()));
    }
}
