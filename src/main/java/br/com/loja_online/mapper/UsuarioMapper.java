package br.com.loja_online.mapper;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Produto;
import br.com.loja_online.model.Usuario;

import java.util.List;
import java.util.ArrayList;

public class UsuarioMapper {
    public static UsuarioDTO paraDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(
                null,
                usuario.getNome(),
                usuario.getTelefone(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                usuario.getGenero(),
                usuario.getFoto(),
                usuario.getTipo(),
                usuario.getLogins() != null ? usuario.getLogins() : new ArrayList<>(),
                usuario.getCartoes() != null ? usuario.getCartoes() : new ArrayList<>(),
                usuario.getEnderecos() != null ? usuario.getEnderecos() : new ArrayList<>()
        );
    }


    public static Usuario paraUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            return null;
        }

        return Usuario.builder()
                .nome(usuarioDTO.nome())
                .telefone(usuarioDTO.telefone())
                .email(usuarioDTO.email())
                .dataNascimento(usuarioDTO.dataNascimento())
                .genero(usuarioDTO.genero())
                .foto(usuarioDTO.foto())
                .tipo(usuarioDTO.tipo())
                .logins(usuarioDTO.logins() != null ? usuarioDTO.logins() : new ArrayList<>())
                .cartoes(usuarioDTO.cartoes() != null ? usuarioDTO.cartoes() : new ArrayList<>())
                .enderecos(usuarioDTO.enderecos() != null ? usuarioDTO.enderecos() : new ArrayList<>())
                .build();

    }
}