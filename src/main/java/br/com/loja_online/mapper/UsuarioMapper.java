package br.com.loja_online.mapper;

import java.util.List;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.model.Usuario;

public class UsuarioMapper {

    public static UsuarioResponseDTO paraDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        List<EnderecoDTO> enderecos = usuario.getEnderecos() != null
                ? usuario.getEnderecos().stream().map(EnderecoMapper::paraDto).toList()
                : java.util.Collections.emptyList();

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .telefone(usuario.getTelefone())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .dataNascimento(usuario.getDataNascimento())
                .genero(usuario.getGenero())
                .foto(usuario.getFoto())
                .tipo(usuario.getTipo())
                .enderecos(enderecos)
                .build();
    }

    public static Usuario paraUsuario(UsuarioRequestDTO usuarioDTO) {
        if (usuarioDTO == null) {
            return null;
        }

        Usuario usuario = Usuario.builder()
                .nome(usuarioDTO.getNome())
                .telefone(usuarioDTO.getTelefone())
                .email(usuarioDTO.getEmail())
                .cpf(usuarioDTO.getCpf())
                .dataNascimento(usuarioDTO.getDataNascimento())
                .genero(usuarioDTO.getGenero())
                .foto(usuarioDTO.getFoto())
                .tipo(usuarioDTO.getTipo())
                .build();

        if (usuarioDTO.getEnderecos() != null) {
            usuarioDTO.getEnderecos().forEach(endereco -> endereco.setUsuario(usuario));
            usuario.setEnderecos(usuarioDTO.getEnderecos());
        }

        return usuario;
    }
}
