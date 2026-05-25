package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final LoginRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository repository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginDTO buscarPorLogin(String login) {
        return repository.findByLogin(login)
                .map(LoginMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));
    }
}