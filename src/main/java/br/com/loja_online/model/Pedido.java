package br.com.loja_online.model;

import br.com.loja_online.model.enums.PedidoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tb_pedido")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> listaProdutos;

    private BigDecimal valorTotal;

    private BigDecimal valorFrete;

    @Enumerated(value = EnumType.STRING)
    private PedidoStatus status;

    private String codigoRastreio;
}
