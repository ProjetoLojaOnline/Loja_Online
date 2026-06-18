package br.com.loja_online.builder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;

public class CartaoBuilder {

    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private Long numeroCartao;
    private String nomeCartao;
    private Date dataValidade;
    private Integer cvv;
    private Boolean defaultCard;

    private CartaoBuilder() {}

    public static CartaoBuilder padrao() {
        CartaoBuilder b = new CartaoBuilder();
        b.numeroCartao = Long.parseLong(
                faker.finance().creditCard().replaceAll("[^0-9]", "").substring(0, 16));
        b.nomeCartao = faker.name().fullName();
        b.dataValidade = Date.valueOf(LocalDate.now().plusYears(2));
        b.cvv = faker.number().numberBetween(100, 999);
        b.defaultCard = true;
        return b;
    }

    public CartaoBuilder comNumeroCartao(Long numeroCartao) {
        this.numeroCartao = numeroCartao;
        return this;
    }

    public CartaoBuilder comNomeCartao(String nomeCartao) {
        this.nomeCartao = nomeCartao;
        return this;
    }

    public CartaoBuilder comDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
        return this;
    }

    public CartaoBuilder comCvv(Integer cvv) {
        this.cvv = cvv;
        return this;
    }

    public CartaoBuilder comDefaultCard(Boolean defaultCard) {
        this.defaultCard = defaultCard;
        return this;
    }

    public CartaoDTO buildDto() {
        return CartaoDTO.builder()
                .numeroCartao(numeroCartao)
                .nomeCartao(nomeCartao)
                .dataValidade(dataValidade)
                .cvv(cvv)
                .defaultCard(defaultCard)
                .build();
    }

    public Cartao buildModel() {
        return Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .nomeCartao(nomeCartao)
                .dataValidade(dataValidade)
                .cvv(cvv)
                .defaultCard(defaultCard)
                .build();
    }
}
