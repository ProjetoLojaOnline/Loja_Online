package br.com.loja_online.dto;


import br.com.loja_online.model.Cartao;
import br.com.loja_online.model.Endereco;

import java.util.List;

public record UsuarioDTO(
        String nome,
        String telefone,
        String email,
        String dataNascimento,
        String genero,
        String foto,
        String tipo,
        List<Cartao> cartoes,
        List<Endereco> enderecos
) {
    public UsuarioDTO(
            String nome,
            String telefone,
            String email,
            String dataNascimento,
            String genero,
            String foto,
            String tipo,
            List<Cartao> cartoes,
            List<Endereco> enderecos
    ) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.foto = foto;
        this.tipo = tipo;
        this.cartoes = cartoes;
        this.enderecos = enderecos;
    }

}
