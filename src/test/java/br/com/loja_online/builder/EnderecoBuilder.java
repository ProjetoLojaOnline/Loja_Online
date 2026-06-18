package br.com.loja_online.builder;

import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.model.Endereco;

public class EnderecoBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

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
        EnderecoBuilder b = new EnderecoBuilder();
        b.logradouro = faker.address().streetName();
        b.numero = faker.number().numberBetween(1, 9999);
        b.bairro = faker.address().cityName();
        b.complemento = "Apto " + faker.number().numberBetween(1, 200);
        b.referencia = "Próximo ao " + faker.address().cityName();
        b.cep = faker.numerify("#####") + "-" + faker.numerify("###");
        b.cidade = faker.address().city();
        b.estado = faker.address().stateAbbr();
        return b;
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
        Endereco e = new Endereco(logradouro, numero, bairro, complemento, referencia, cep, cidade, estado);
        return e;
    }
}
