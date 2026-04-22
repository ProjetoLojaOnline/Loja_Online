package br.com.loja_online.model;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cartao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cartao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long numeroCartao;
    private String nomeCartao;
    private Date dataValidade;
    private Integer cvv;
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    private Boolean defaultCard;
}


