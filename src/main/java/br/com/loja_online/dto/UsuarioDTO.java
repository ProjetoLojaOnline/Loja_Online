package br.com.loja_online.dto;


import br.com.loja_online.model.Endereco;

public record UsuarioDTO(
                         String nome,
                         String email,
                         String telefone,
                         String dataNascimento,
                         String genero,
                         String foto,
                         String tipo,
                         Endereco endereco) {

    public UsuarioDTO(String nome,
                      String email,
                      String telefone,
                      String dataNascimento,
                      String genero,
                      String foto,
                      String tipo) {

        this(nome, email, telefone, dataNascimento, genero, foto, tipo, null);
    }
}
