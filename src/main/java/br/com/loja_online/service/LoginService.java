package br.com.loja_online.service;

import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private LoginRepository repository;

    public Login buscarPorLogin(String login) {
        Optional<Login> loginOptional = repository.findByLogin(login);
        return loginOptional.orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));
    }
}