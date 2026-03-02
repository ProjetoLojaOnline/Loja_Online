package br.com.loja_online.mapper;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.*;

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
                usuario.getLogin() != null ? usuario.getLogin().getLogin() : null,
                null,
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
                .login(Login.builder()
                        .login(usuarioDTO.login())
                        .senha(usuarioDTO.senha())
                        .build())
                .cartoes(usuarioDTO.cartoes() != null ? usuarioDTO.cartoes() : new ArrayList<>())
                .enderecos(usuarioDTO.enderecos() != null ? usuarioDTO.enderecos() : new ArrayList<>())
                .build();

    }
}