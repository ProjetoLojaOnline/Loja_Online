package br.com.loja_online.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Entity
@Table(name="tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Usuario{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nome;
    private String login;
    private String telefone;
    private String email;
    private String cpf;
    private String dataNAscimento;
    private String genero;
    @OneToMany(mappedBy = "id_usuario", cascade = CascadeType.ALL)
    private Cartao cartao;
    @OneToMany(mappedBy = "id_usuario", cascade = CascadeType.ALL)
    private Endereco endereco;
    private String foto;
    private String tipo;
}