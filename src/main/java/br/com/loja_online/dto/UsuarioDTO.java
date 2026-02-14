package br.com.loja_online.dto;


import br.com.loja_online.model.Endereco;


public record UsuarioDTO(Integer id,
                         String nome,
                         String email,
                         String telefone,
                         String cpf,
                         String dataNascimento,
                         String genero,
                         String foto,
                         String tipo) {
        public UsuarioDTO(String nome,
                          String email,
                          String telefone,
                          String cpf,
                          String dataNascimento,
                          String genero,
                          String foto,
                          String tipo) {
            this(null, nome, email, telefone, cpf, dataNascimento, genero, foto, tipo);
        }

}
