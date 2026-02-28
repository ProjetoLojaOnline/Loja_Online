package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private LoginRepository repository;

    private final PasswordEncoder passwordEncoder;
    public LoginService(LoginRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    public Login buscarPorLogin(String login) {
        Optional<Login> loginOptional = repository.findByLogin(login);
        return loginOptional.orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));

    }
    public @Valid LoginDTO salvar(LoginDTO dto) {
        if(repository.existsByLogin(dto.login())){
            throw new RuntimeException("Login já existe");
        }
        Login login = Login.builder().login(dto.login())
                .senha(passwordEncoder.encode(dto.senha()))
                .build();
        return repository.save(login);
    }
}