package br.com.loja_online.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.dto.VerificacaoCpfPendenteDTO;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByLogin_Login(String login);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByTelefone(String telefone);

    /** PENDENTE e REJEITADO cuja próxima verificação já venceu (null = nunca verificado). */
    @Query(
            """
            select new br.com.loja_online.dto.VerificacaoCpfPendenteDTO(
                u.id, u.cpf, u.statusVerificacaoCpf, u.tentativasVerificacaoCpf)
            from Usuario u
            where u.statusVerificacaoCpf in :status
              and (u.proximaVerificacaoCpfEm is null or u.proximaVerificacaoCpfEm <= :agora)
            order by u.proximaVerificacaoCpfEm asc nulls first, u.id asc
            """)
    List<VerificacaoCpfPendenteDTO> findParaVerificarCpf(
            List<StatusVerificacaoCpf> status, Instant agora, Pageable pageable);

    /**
     * Grava o resultado da verificação só se o CPF não mudou desde a consulta (ex.: o usuário corrigiu o CPF
     * enquanto o provedor respondia). Retorna 0 quando não atualizou.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            """
            update Usuario u
            set u.statusVerificacaoCpf = :status,
                u.tentativasVerificacaoCpf = :tentativas,
                u.proximaVerificacaoCpfEm = :proximaVerificacao
            where u.id = :id and u.cpf = :cpf
            """)
    int atualizarVerificacaoCpf(
            Long id, String cpf, StatusVerificacaoCpf status, int tentativas, Instant proximaVerificacao);
}
