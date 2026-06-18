package br.com.loja_online.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import br.com.loja_online.model.Endereco;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(min = 10, max = 11, message = "Telefone deve ter entre 10 e 11 dígitos")
    private String telefone;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF deve ter exatamente 11 dígitos")
    private String cpf;

    private String dataNascimento;
    private String genero;
    private String foto;
    private String tipo;

    @Builder.Default
    private List<Endereco> enderecos = new ArrayList<>();
}
