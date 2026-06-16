package br.com.loja_online.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Usuario {

    public Usuario() {
        this.cartoes = new java.util.ArrayList<>();
        this.enderecos = new java.util.ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Login login;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cartao> cartoes = new java.util.ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new java.util.ArrayList<>();

}
