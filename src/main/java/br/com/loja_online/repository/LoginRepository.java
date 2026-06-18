package br.com.loja_online.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.loja_online.model.Login;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findByLogin(String login);

    boolean existsByLogin(String login);
}
