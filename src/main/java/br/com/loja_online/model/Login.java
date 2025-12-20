package br.com.loja_online.model;

import jakarta.persistence.*;

@Entity
@Table(name = "login")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 120)
    private String username;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    protected Login() {}

    public Login(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }

    // getter/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
