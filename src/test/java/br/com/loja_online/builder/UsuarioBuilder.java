package br.com.loja_online.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;

import net.datafaker.Faker;

public class UsuarioBuilder {

    private static final Random RANDOM = new Random();
    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private List<EnderecoDTO> enderecos;
    private String login;
    private String senha;

    private UsuarioBuilder() {}

    private static String gerarCpfValido() {
        int[] d = new int[11];
        do {
            for (int i = 0; i < 9; i++) d[i] = RANDOM.nextInt(10);
        } while (Arrays.stream(d, 0, 9).distinct().count() == 1);

        d[9] = dv(d, 9);
        d[10] = dv(d, 10);

        StringBuilder sb = new StringBuilder();
        for (int x : d) sb.append(x);
        return sb.toString();
    }

    private static int dv(int[] d, int qtd) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) soma += d[i] * (qtd + 1 - i);
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public static UsuarioBuilder padrao() {
        UsuarioBuilder builder = new UsuarioBuilder();
        builder.nome = faker.name().fullName();
        builder.email = faker.internet().emailAddress();
        builder.cpf = gerarCpfValido();
        builder.telefone = faker.numerify("##########");
        builder.enderecos = List.of(EnderecoBuilder.padrao().buildDto());
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
                .enderecos(enderecos)
                .build();
        LoginDTO loginDto = new LoginDTO(login, senha);
        return new UsuarioCadastroWrapper(usuario, loginDto);
    }
}
