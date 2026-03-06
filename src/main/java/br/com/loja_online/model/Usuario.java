package br.com.loja_online.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {

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

    // "Eu sou o dono do Login. O mapa está no campo 'usuario' da classe Login"
    // 1. Relacionamento 1:1 com Login
    // O mappedBy="usuario" diz que o campo 'usuario' está lá na classe Login
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Login login;

    // "Eu sou o dono dos Cartões. O mapa está no campo 'usuario' da classe Cartao"
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cartao> cartoes = new java.util.ArrayList<>();

    // "Eu sou o dono dos Endereços. O mapa está no campo 'usuario' da classe
    // Endereco"
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new java.util.ArrayList<>();

}
