package br.com.loja_online.dto;

import java.util.ArrayList;
import java.util.List;

import br.com.loja_online.model.enums.StatusVerificacaoCpf;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;
    private StatusVerificacaoCpf statusVerificacaoCpf;

    @Builder.Default
    private List<EnderecoDTO> enderecos = new ArrayList<>();
}
