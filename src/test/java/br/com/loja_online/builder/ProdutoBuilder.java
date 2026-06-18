package br.com.loja_online.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;

public class ProdutoBuilder {

    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private Integer id;
    private String nome;
    private String descricao;
    private String categoria;
    private Integer quantidade;
    private BigDecimal preco;
    private String cor;

    private ProdutoBuilder() {}

    public static ProdutoBuilder padrao() {
        ProdutoBuilder b = new ProdutoBuilder();
        b.id = null;
        b.nome = faker.commerce().productName();
        b.descricao = faker.lorem().sentence();
        b.categoria = faker.commerce().department();
        b.quantidade = faker.number().numberBetween(0, 100);
        b.preco = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 999))
                .setScale(2, RoundingMode.HALF_UP);
        b.cor = faker.color().name();
        return b;
    }

    public ProdutoBuilder comId(Integer id) {
        this.id = id;
        return this;
    }

    public ProdutoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ProdutoBuilder comPreco(BigDecimal preco) {
        this.preco = preco;
        return this;
    }

    public ProdutoDTO buildDto() {
        return new ProdutoDTO(id, nome, descricao, categoria, quantidade, preco, cor);
    }

    public Produto buildModel() {
        return new Produto(id == null ? 1 : id, nome, descricao, categoria, quantidade, preco, cor);
    }
}
