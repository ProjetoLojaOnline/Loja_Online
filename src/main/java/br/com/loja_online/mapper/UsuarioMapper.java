package br.com.loja_online.mapper;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.Produto;
import br.com.loja_online.model.Usuario;

public class UsuarioMapper {

        public static UsuarioDTO paraDto(Usuario usuario) {
            return new UsuarioDTO(usuario.getNome(), usuario.getTelefone(),
                    usuario.getEmail(), usuario.getDataNascimento(), usuario.getCpf(), usuario.getGenero(),
                    usuario.getFoto(), usuario.getTipo());
        }

        public static Usuario paraUsuario(UsuarioDTO usuarioDTO) {
            return new Usuario(usuarioDTO.nome(), usuarioDTO.telefone(), usuarioDTO.email(),
                    usuarioDTO.dataNascimento(), usuarioDTO.cpf(), usuarioDTO.genero(), usuarioDTO.foto(), usuarioDTO.tipo()
            );
        }
    }

