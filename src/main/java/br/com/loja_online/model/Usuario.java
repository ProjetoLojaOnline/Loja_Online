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
    private String dataNascimento;
    private String cpf;
    private String senha;
    private String genero;
    private String foto;
    private String tipo;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_usuario")
    private List<Cartao> cartoes;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_usuario")
    private List<Endereco> enderecos;

    public Usuario(String nome, String telefone, String email, String s, String cpf, String genero, String foto, String tipo) {
    }
}
