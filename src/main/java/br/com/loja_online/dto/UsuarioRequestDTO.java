package br.com.loja_online.dto;

import br.com.loja_online.model.Cartao;
import br.com.loja_online.model.Endereco;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequestDTO {

    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;
    @Builder.Default
    private List<Cartao> cartoes = new ArrayList<>();
    @Builder.Default
    private List<Endereco> enderecos = new ArrayList<>();
}
