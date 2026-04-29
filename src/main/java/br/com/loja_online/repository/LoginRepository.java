package br.com.loja_online.repository;

import br.com.loja_online.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {

    UserDetails findByLogin(String login);

    Optional<Login> findLoginByLogin(String login);

    boolean existsByLogin(String login);
}