package br.com.loja_online.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "senha", nullable = false, length = 255)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
    private String senha;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
