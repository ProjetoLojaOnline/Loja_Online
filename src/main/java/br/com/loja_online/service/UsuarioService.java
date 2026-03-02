package br.com.loja_online.service;

import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          LoginRepository loginRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public @Valid UsuarioDTO insert(@Valid UsuarioDTO usuarioDTO) {
        if (loginRepository.existsByLogin(usuarioDTO.login())) {
            throw new RuntimeException("Este login já está em uso!");
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new RuntimeException("Este e-mail já está cadastrado!");
        }

        Usuario novoUsuario = UsuarioMapper.paraUsuario(usuarioDTO);
        novoUsuario.setId(null);

        // Criptografia da senha
        if (novoUsuario.getLogin() != null) {
            String senhaCriptografada = passwordEncoder.encode(novoUsuario.getLogin().getSenha());
            novoUsuario.getLogin().setSenha(senhaCriptografada);
        }

        novoUsuario = usuarioRepository.save(novoUsuario);
        return UsuarioMapper.paraDTO(novoUsuario);
    }

    public UsuarioDTO findByLogin(String login) {
        Usuario usuario = usuarioRepository.findByLogin_Login(login)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + login));
        return UsuarioMapper.paraDTO(usuario);
    }

    public UsuarioDTO findById(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ObjectNotFoundException("Usuário não encontrado para deletar");
        }
        usuarioRepository.deleteById(id);
    }

    public void atualizaUsuario(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new ObjectNotFoundException("Usuário não encontrado para atualizar");
        }
        usuarioRepository.save(usuario);
    }
}
