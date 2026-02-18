package br.com.loja_online.dto;



public record UsuarioDTO(
        String nome,
        String telefone,
        String email,
        String dataNascimento,
        String genero,
        String foto,
        String tipo
) {
    public UsuarioDTO(
            String nome,
            String telefone,
            String email,
            String dataNascimento,
            String genero,
            String foto,
            String tipo
    ) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.foto = foto;
        this.tipo = tipo;
    }

}
