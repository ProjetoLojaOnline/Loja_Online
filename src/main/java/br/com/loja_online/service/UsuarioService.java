package br.com.loja_online.service;

import br.com.loja_online.dto.AutenticacaoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioDTO findByLogin(String email){
        Usuario usuario = usuarioRepository.findByLogin(email)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + email));
        return UsuarioMapper.paraDTO(usuario);

    }
    public UsuarioDTO findById(Integer id){
        Usuario usuario =usuarioRepository.findById(id.intValue())
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
        return UsuarioMapper.paraDTO(usuario);
    }
    public void deleteById(Integer id){
        usuarioRepository.deleteById(id.intValue());
    }

    public void atualizaUsuario(Usuario usuario){
        if (!usuarioRepository.existsById(usuario.getId().intValue())) {
            throw new ObjectNotFoundException("Usuário não encontrado para atualizar");
        }
        usuarioRepository.save(usuario);
    }
    public UsuarioDTO criar(@Valid AutenticacaoDTO autenticacaoDTO) {
        Usuario novoUsuario = UsuarioMapper.paraUsuario(autenticacaoDTO);
        novoUsuario.setId(null);
        novoUsuario = usuarioRepository.save(novoUsuario);
        return UsuarioMapper.paraDTO(novoUsuario);
    }
}
