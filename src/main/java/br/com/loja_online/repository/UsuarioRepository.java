package br.com.loja_online.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.loja_online.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByLogin_Login(String login);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByTelefone(String telefone);
}
