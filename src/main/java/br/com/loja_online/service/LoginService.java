package br.com.loja_online.service;

import br.com.loja_online.domain.login.Login;
import br.com.loja_online.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {

    @Autowired
    private LoginRepository repository;

    public List<Login> listaTodos() {
        return (List<Login>) repository.findAll();

    }
}