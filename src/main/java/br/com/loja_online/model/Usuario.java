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
    private String senha;
    private String email;
    private String telefone;
    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_usuario")
    private List<Cartao> cartoes;

    public Usuario(String nome,
                   String senha,
                   String email,
                   String telefone,
                   String dataNascimento,
                   String genero,
                   String foto,
                   String tipo) {

        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.foto = foto;
        this.tipo = tipo;

    }
}
