package br.com.loja_online.repository;

import br.com.loja_online.domain.login.Login;
import br.com.loja_online.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
<<<<<<< HEAD:src/main/java/br/com/loja_online/repository/UsuarioRepository.java
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByLogin(String login);
=======
public interface LoginRepository extends JpaRepository<Login, Long> {
    boolean existsByUsername(String username);
>>>>>>> main:src/main/java/br/com/loja_online/repository/LoginRepository.java
}
