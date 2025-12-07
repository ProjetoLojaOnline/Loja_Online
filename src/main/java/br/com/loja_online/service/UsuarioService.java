package br.com.loja_online.service;

import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    public Optional<Usuario> findByLogin(String login){
        return usuarioRepository.findByLogin(login);
    }
    public Usuario findById(Integer id){
        return usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
    public void deleteById(Integer id){
        usuarioRepository.deleteById(id);
    }
    public void criaUsuario(Usuario usuario){
        usuarioRepository.save(usuario);
    }
    public void atualizaUsuario(Usuario usuario){
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuário não encontrado para atualizar");
        }
        usuarioRepository.save(usuario);
    }
}
