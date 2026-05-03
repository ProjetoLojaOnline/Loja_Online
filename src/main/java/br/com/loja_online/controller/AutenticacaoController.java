package br.com.loja_online.controller;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.security.DadosToken;
import br.com.loja_online.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<DadosToken> efetuarLogin(@RequestBody @Valid LoginDTO dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var autenticacao = authenticationManager.authenticate(token);

        String tokenJWT = tokenService.gerarToken((Login) autenticacao.getPrincipal());

        return ResponseEntity.ok(new DadosToken(tokenJWT));
    }
}