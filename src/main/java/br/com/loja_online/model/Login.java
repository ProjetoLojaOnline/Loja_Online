package br.com.loja_online.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "login")
public class Login {

    @Id
    @Column(name = "login", nullable = false, unique = true, length = 120)
    private String login;

    @Column(name = "senha", nullable = false, length = 255)

    private String senha;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

}