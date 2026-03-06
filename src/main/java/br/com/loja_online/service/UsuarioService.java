package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::paraDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponseDTO insert(@Valid UsuarioRequestDTO usuarioDTO, @Valid LoginDTO loginDTO) {
        if (loginRepository.existsByLogin(loginDTO.login())) {
            throw new RuntimeException("Este login já está em uso!");
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("Este e-mail já está cadastrado!");
        }

        Usuario novoUsuario = UsuarioMapper.paraUsuario(usuarioDTO);
        novoUsuario.setId(null);

        Login login = new Login();
        login.setLogin(loginDTO.login());
        login.setSenha(passwordEncoder.encode(loginDTO.senha()));
        login.setUsuario(novoUsuario);
        novoUsuario.setLogin(login);

        novoUsuario = usuarioRepository.save(novoUsuario);
        return UsuarioMapper.paraDTO(novoUsuario);
    }

    public UsuarioResponseDTO findByLogin(String login) {
        Usuario usuario = usuarioRepository.findByLogin_Login(login)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + login));
        return UsuarioMapper.paraDTO(usuario);
    }

    public UsuarioResponseDTO findById(Long id) {
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

    @Transactional
    public UsuarioResponseDTO atualizaUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado"));

        // Atualiza os campos permitidos usando os dados do DTO
        usuarioExistente.setNome(dto.getNome());
        usuarioExistente.setEmail(dto.getEmail());
        usuarioExistente.setTelefone(dto.getTelefone());
        usuarioExistente.setCpf(dto.getCpf());

        usuarioExistente = usuarioRepository.save(usuarioExistente);
        return UsuarioMapper.paraDTO(usuarioExistente);
    }
}