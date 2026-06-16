package br.com.loja_online.mapper;

import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.model.*;


public class UsuarioMapper {
    public static UsuarioResponseDTO paraDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
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
                .cartoes(usuario.getCartoes())
                .enderecos(usuario.getEnderecos())
                .build();
    }

    public static Usuario paraUsuario(UsuarioRequestDTO usuarioDTO) {
        if (usuarioDTO == null)
            return null;

        // 1. Criamos o "Pai" (Usuario) primeiro
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

        // 3. Vinculamos os Cartões e avisamos o dono para cada um
        if (usuarioDTO.getCartoes() != null) {
            usuarioDTO.getCartoes().forEach(cartao -> cartao.setUsuario(usuario));
            usuario.setCartoes(usuarioDTO.getCartoes());
        }

        // 4. Vinculamos os Endereços e avisamos o dono para cada um
        if (usuarioDTO.getEnderecos() != null) {
            usuarioDTO.getEnderecos().forEach(endereco -> endereco.setUsuario(usuario));
            usuario.setEnderecos(usuarioDTO.getEnderecos());
        }

        return usuario;
    }

}
