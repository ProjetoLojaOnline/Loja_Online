package br.com.loja_online.repository;

import br.com.loja_online.domain.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {
    boolean existsByUsername(String username);
}
