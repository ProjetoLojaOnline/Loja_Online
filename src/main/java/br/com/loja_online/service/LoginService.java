package br.com.loja_online.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.security.TokenService;
import br.com.loja_online.service.exceptions.AuthenticationException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginRepository loginRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public LoginDTO buscarPorLogin(String login) {
        return loginRepository
                .findByLogin(login)
                .map(LoginMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));
    }

    public String login(String email, String senha) {
        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas"));

        Login login = usuario.getLogin();
        if (login == null || !passwordEncoder.matches(senha, login.getSenha())) {
            throw new AuthenticationException("Credenciais inválidas");
        }

        return tokenService.gerarToken(email);
    }
}
