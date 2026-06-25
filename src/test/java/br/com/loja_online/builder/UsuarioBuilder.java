package br.com.loja_online.builder;

import java.util.Locale;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;

import net.datafaker.Faker;

public class UsuarioBuilder {

    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String login;
    private String senha;

    private UsuarioBuilder() {}

    public static UsuarioBuilder padrao() {
        UsuarioBuilder builder = new UsuarioBuilder();
        builder.nome = faker.name().fullName();
        builder.email = faker.internet().emailAddress();
        builder.cpf = faker.numerify("###########");
        builder.telefone = faker.numerify("##########");
        builder.login = "user" + faker.number().digits(6);
        builder.senha = faker.internet().password(6, 20, true, false);
        return builder;
    }

    public UsuarioBuilder comEmail(String email) {
        this.email = email;
        return this;
    }

    public UsuarioBuilder comSenha(String senha) {
        this.senha = senha;
        return this;
    }

    public UsuarioBuilder comLogin(String login) {
        this.login = login;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getLogin() {
        return login;
    }

    public UsuarioCadastroWrapper buildWrapper() {
        UsuarioRequestDTO usuario = UsuarioRequestDTO.builder()
                .nome(nome)
                .email(email)
                .cpf(cpf)
                .telefone(telefone)
                .build();
        LoginDTO loginDto = new LoginDTO(login, senha);
        return new UsuarioCadastroWrapper(usuario, loginDto);
    }
}
