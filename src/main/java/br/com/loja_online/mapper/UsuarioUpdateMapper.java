package br.com.loja_online.mapper;

import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.model.Usuario;

public class UsuarioUpdateMapper {

    public static void updateUsuarioDTO(UsuarioUpdateDTO dto, Usuario user) {
        if (dto == null || user == null) {
            return;
        }

        user.setNome(dto.nome());
        user.setTelefone(dto.telefone());
        user.setFoto(dto.foto());
        user.setGenero(dto.genero());
    }
}
