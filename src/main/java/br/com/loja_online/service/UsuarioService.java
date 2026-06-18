package br.com.loja_online.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.mapper.UsuarioUpadateMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ConflictException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

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
            throw new ConflictException("Este login já está em uso!");
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new ConflictException("Este e-mail já está cadastrado!");
        }
        if(usuarioRepository.existsByCpf(usuarioDTO.getCpf())) {
            throw new ConflictException("Este CPF já está cadastrado!");
        }
        if(usuarioRepository.existsByTelefone(usuarioDTO.getTelefone())) {
            throw new ConflictException("Esse telefone já está em uso!");
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

    public UsuarioResponseDTO findById(@NonNull Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    public void deleteById(@NonNull Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ObjectNotFoundException("Usuário não encontrado para deletar");
        }
        usuarioRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    @Transactional
    public UsuarioResponseDTO atualizaUsuario(@NonNull Long id, @Valid UsuarioUpdateDTO dto) {
        Usuario dados = usuarioRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuario Não encontrado com o ID: " + id));
        UsuarioUpadateMapper.updateUsuarioDTO(dto, dados);
        Usuario salvo = usuarioRepository.save(dados);

        return UsuarioMapper.paraDTO(salvo);
    }
}
