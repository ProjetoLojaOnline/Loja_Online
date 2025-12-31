package br.com.loja_online.service;

import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    public Usuario findByLogin(String login){
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + login));
    }
    public Usuario findById(Long id){
        return usuarioRepository.findById(id.intValue())
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
    }
    public void deleteById(Long id){
        usuarioRepository.deleteById(id.intValue());
    }
    public void criaUsuario(Usuario usuario){
        usuarioRepository.save(usuario);
    }
    public void atualizaUsuario(Usuario usuario){
        if (!usuarioRepository.existsById(usuario.getId().intValue())) {
            throw new ObjectNotFoundException("Usuário não encontrado para atualizar");
        }
        usuarioRepository.save(usuario);
    }
}
