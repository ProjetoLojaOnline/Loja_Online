package br.com.loja_online.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private LoginRepository repository;

    private static final Logger log = LoggerFactory.getLogger(AutenticacaoService.class);

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Tentativa de autenticação para usuário: {}", username);
        return repository.findByLogin(username)
                .orElseThrow(() -> {
                log.warn("Falha de autenticação: usuário '{}' não encontrado", username);
                return new AuthenticationException("Credenciais inválidas");

                });
    }
}