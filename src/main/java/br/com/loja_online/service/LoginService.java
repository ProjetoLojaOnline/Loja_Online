package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.AuthenticationException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private LoginRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository repository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
//resolver
    public LoginDTO buscarPorLogin(String login) {
        return repository.findByLogin(login)
                .map(LoginMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));
    }
//
    public String login(String email, String senha) {
        // Buscar o usuário no banco pelo email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new AuthenticationException("Credenciais inválidas");
        }
        Usuario usuario = usuarioOpt.get();
        Login login = usuario.getLogin();

        if (login == null) {
            throw new AuthenticationException("Credenciais inválidas");
        }
        // Validar a senha usando PasswordEncoder
        if (!passwordEncoder.matches(senha, login.getSenha())) {
            throw new AuthenticationException("Credenciais inválidas");
        }
        return "Login realizado com sucesso";
    }
}
