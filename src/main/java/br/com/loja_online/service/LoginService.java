package br.com.loja_online.service;

import org.springframework.stereotype.Service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@Service
public class LoginService {

    private final LoginRepository repository;

    public LoginService(LoginRepository repository) {
        this.repository = repository;
    }

    public LoginDTO buscarPorLogin(String login) {
        return repository.findByLogin(login)
                .map(LoginMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Login não encontrado: " + login));
    }
}
