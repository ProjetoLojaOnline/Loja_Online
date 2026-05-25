package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.security.DadosToken;
import br.com.loja_online.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AutenticacaoController (AuthenticationManager authenticationManager,
                                   TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;

    }

    @PostMapping
    public ResponseEntity<DadosToken> efetuarLogin(@RequestBody @Valid LoginDTO dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var autenticacao = authenticationManager.authenticate(token);

        if (!(autenticacao.getPrincipal() instanceof Login login)) {
            throw new RuntimeException("Erro inesperado na autenticação");
        }

        String tokenJWT = tokenService.gerarToken(login);
        return ResponseEntity.ok(new DadosToken(tokenJWT));
    }
}

