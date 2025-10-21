package br.com.loja_online.service;

import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    public Usuario findByLogin(String login){}
    public Usuario findById(Integer id){}
    public void deleteById(Integer id){}
    public void criaUsuario(Usuario usuario){}
    public void atualizaUsuario(Usuario usuario){}
}
