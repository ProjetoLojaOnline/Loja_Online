package br.com.loja_online.repository;

import br.com.loja_online.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {

    boolean existsByLogin(String login);

    Optional<Login> findByLogin(String login);
}
