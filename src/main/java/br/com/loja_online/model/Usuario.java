package br.com.loja_online.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import br.com.loja_online.model.enums.StatusVerificacaoCpf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Usuario {

    public Usuario() {
        this.enderecos = new ArrayList<>();
        this.statusVerificacaoCpf = StatusVerificacaoCpf.PENDENTE;
    }

    @PostLoad
    private void inicializarColecoes() {
        if (this.enderecos == null) {
            this.enderecos = new ArrayList<>();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String telefone;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String cpf;

    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Login login;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    // O @ColumnDefault faz o ddl-auto=update preencher as linhas que já existem no banco
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDENTE'")
    private StatusVerificacaoCpf statusVerificacaoCpf = StatusVerificacaoCpf.PENDENTE;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int tentativasVerificacaoCpf;

    // null = verificar assim que o job rodar
    private Instant proximaVerificacaoCpfEm;

    /** CPF rejeitado não pode finalizar compra. PENDENTE pode, para não travar a conversão. */
    public boolean podeComprar() {
        return statusVerificacaoCpf != StatusVerificacaoCpf.REJEITADO;
    }

    /** Vender exige CPF aprovado (a verificação do CNPJ do perfil vendedor é tratada à parte). */
    public boolean podeVender() {
        return statusVerificacaoCpf == StatusVerificacaoCpf.APROVADO;
    }
}
