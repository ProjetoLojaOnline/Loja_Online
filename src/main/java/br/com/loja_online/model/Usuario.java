package br.com.loja_online.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Id;

@Entity
@Table(name="tb_usuario")
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
    private Cartao cartao;
    private Endereco endereco;
    private String foto;

    public Usuario(String nome, String login, String telefone, String email, String cpf, String dataNAscimento, String genero, Cartao cartao, Endereco endereco, String foto) {
        this.nome = nome;
        this.login = login;
        this.telefone = telefone;
        this.email = email;
        this.cpf = cpf;
        this.dataNAscimento = dataNAscimento;
        this.genero = genero;
        this.cartao = cartao;
        this.endereco = endereco;
        this.foto = foto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getDataNAscimento() {
        return dataNAscimento;
    }

    public void setDataNAscimento(String dataNAscimento) {
        this.dataNAscimento = dataNAscimento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}