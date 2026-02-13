package br.com.loja_online.mapper;

import br.com.loja_online.dto.AutenticacaoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.Usuario;
import jakarta.validation.Valid;

public class UsuarioMapper {

        public static Usuario paraUsuario(@Valid AutenticacaoDTO autenticacaoDTO) {
            return new Usuario(autenticacaoDTO.nome(), autenticacaoDTO.senha(), autenticacaoDTO.email(),
                    autenticacaoDTO.telefone(), autenticacaoDTO.cpf(), autenticacaoDTO.dataNascimento(),
                    autenticacaoDTO.genero(), autenticacaoDTO.foto(), autenticacaoDTO.tipo());
        }
        public static UsuarioDTO paraDTO(Usuario usuario) {
            return new UsuarioDTO(usuario.getNome(), usuario.getEmail(), usuario.getTelefone(),
                    usuario.getCpf(), usuario.getDataNascimento(), usuario.getGenero(),
                    usuario.getFoto(), usuario.getTipo());
        }
    }

