package br.com.loja_online.builder;

import java.util.Locale;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.model.Endereco;

import net.datafaker.Faker;

public class EnderecoBuilder {

    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private String logradouro;
    private Integer numero;
    private String bairro;
    private String complemento;
    private String referencia;
    private String cep;
    private String cidade;
    private String estado;

    private EnderecoBuilder() {}

    public static EnderecoBuilder padrao() {
        EnderecoBuilder builder = new EnderecoBuilder();
        builder.logradouro = faker.address().streetName();
        builder.numero = faker.number().numberBetween(1, 9999);
        builder.bairro = faker.address().cityName();
        builder.complemento = "Apto " + faker.number().numberBetween(1, 200);
        builder.referencia = "Próximo ao " + faker.address().cityName();
        builder.cep = faker.numerify("#####") + "-" + faker.numerify("###");
        builder.cidade = faker.address().city();
        builder.estado = faker.address().stateAbbr();
        return builder;
    }

    public EnderecoBuilder comCep(String cep) {
        this.cep = cep;
        return this;
    }

    public EnderecoBuilder comLogradouro(String logradouro) {
        this.logradouro = logradouro;
        return this;
    }

    public EnderecoDTO buildDto() {
        return new EnderecoDTO(logradouro, numero, bairro, complemento, referencia, cep, cidade, estado);
    }

    public Endereco buildModel() {
        return new Endereco(logradouro, numero, bairro, complemento, referencia, cep, cidade, estado);
    }
}
