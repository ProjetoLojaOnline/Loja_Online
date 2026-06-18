package br.com.loja_online.service;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.mapper.UsuarioUpdateMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.model.enums.Role;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ConflictException;
import br.com.loja_online.service.exceptions.ForbiddenException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository, LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO insert(@Valid UsuarioRequestDTO usuarioDTO, @Valid LoginDTO loginDTO) {
        if (loginRepository.existsByLogin(loginDTO.login())) {
            throw new ConflictException("Este login já está em uso!");
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new ConflictException("Este e-mail já está cadastrado!");
        }
        if (usuarioRepository.existsByCpf(usuarioDTO.getCpf())) {
            throw new ConflictException("Este CPF já está cadastrado!");
        }
        if (usuarioRepository.existsByTelefone(usuarioDTO.getTelefone())) {
            throw new ConflictException("Esse telefone já está em uso!");
        }

        Usuario novoUsuario = UsuarioMapper.paraUsuario(usuarioDTO);
        novoUsuario.setId(null);

        Login login = new Login();
        login.setLogin(loginDTO.login());
        login.setSenha(passwordEncoder.encode(loginDTO.senha()));
        login.setRole(Role.ROLE_USER);
        login.setUsuario(novoUsuario);
        novoUsuario.setLogin(login);

        novoUsuario = usuarioRepository.save(novoUsuario);
        return UsuarioMapper.paraDTO(novoUsuario);
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::paraDTO).toList();
    }

    public UsuarioResponseDTO findByLogin(@NonNull String login) {
        return usuarioRepository
                .findByLogin_Login(login)
                .map(UsuarioMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + login));
    }

    public UsuarioResponseDTO findById(@NonNull Long id) {
        return usuarioRepository
                .findById(id)
                .map(UsuarioMapper::paraDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    public void deleteById(@NonNull Long id, @NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado para deletar"));
        if (!usuario.getEmail().equals(emailAutenticado)) {
            throw new ForbiddenException("Acesso negado: você não pode deletar outro usuário");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioResponseDTO atualizaUsuario(
            @NonNull Long id, @Valid UsuarioUpdateDTO dto, @NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
        if (!usuario.getEmail().equals(emailAutenticado)) {
            throw new ForbiddenException("Acesso negado: você não pode alterar dados de outro usuário");
        }
        UsuarioUpdateMapper.updateUsuarioDTO(dto, usuario);
        return UsuarioMapper.paraDTO(usuarioRepository.save(usuario));
    }
}
