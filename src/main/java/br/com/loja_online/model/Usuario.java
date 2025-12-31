package br.com.loja_online.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String login;
    private String telefone;
    private String email;
    private String cpf;
    private String dataNascimento;
    private String genero;
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "id_usuario")
//    private List<Cartao> cartoes;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_usuario")
    private List<Endereco> enderecos;
    private String foto;
    private String tipo;
}