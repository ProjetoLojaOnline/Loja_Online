package br.com.loja_online.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.AuthenticationException;

@Service
public class AutenticacaoService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(AutenticacaoService.class);

    private final LoginRepository repository;

    public AutenticacaoService(LoginRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        LOG.info("Tentativa de autenticação para usuário: {}", username);
        return repository.findByLogin(username)
                .orElseThrow(() -> {
                    LOG.warn("Falha de autenticação: usuário '{}' não encontrado", username);
                    return new AuthenticationException("Credenciais inválidas");
                });
    }
}
