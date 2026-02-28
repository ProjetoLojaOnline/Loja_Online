package br.com.loja_online.dto;


import br.com.loja_online.model.Cartao;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Login;

import java.util.List;

public record UsuarioDTO(
        Long id,
        String nome,
        String telefone,
        String email,
        String dataNascimento,
        String genero,
        String foto,
        String tipo,
        List<Login> logins,
        List<Cartao> cartoes,
        List<Endereco> enderecos
) {
    public UsuarioDTO(
            Long id,
            String nome,
            String telefone,
            String email,
            String dataNascimento,
            String genero,
            String foto,
            String tipo,
            List<Login> logins,
            List<Cartao> cartoes,
            List<Endereco> enderecos
    ) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.foto = foto;
        this.tipo = tipo;
        this.logins = logins;
        this.cartoes = cartoes;
        this.enderecos = enderecos;
    }

}