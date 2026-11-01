package br.com.loja_online.service;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.dto.CpfCorrecaoDTO;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.dto.validation.CpfValidator;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.mapper.UsuarioUpdateMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.model.enums.Role;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ConflictException;
import br.com.loja_online.service.exceptions.ForbiddenException;
import br.com.loja_online.service.exceptions.MensagensConflito;
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
        // Guarda só os dígitos: "529.982.247-25" e "52998224725" são o mesmo CPF para o unique
        String cpf = CpfValidator.somenteDigitos(usuarioDTO.getCpf());

        if (loginRepository.existsByLogin(loginDTO.login())) {
            throw new ConflictException(MensagensConflito.LOGIN);
        }
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new ConflictException(MensagensConflito.EMAIL);
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ConflictException(MensagensConflito.CPF);
        }
        if (usuarioRepository.existsByTelefone(usuarioDTO.getTelefone())) {
            throw new ConflictException(MensagensConflito.TELEFONE);
        }

        Usuario novoUsuario = UsuarioMapper.paraUsuario(usuarioDTO);
        novoUsuario.setId(null);
        novoUsuario.setCpf(cpf);
        // A verificação na fonte externa é feita depois pelo VerificacaoCpfJob, sem atrasar o cadastro
        novoUsuario.setStatusVerificacaoCpf(StatusVerificacaoCpf.PENDENTE);

        Login login = new Login();
        login.setLogin(loginDTO.login());
        login.setSenha(passwordEncoder.encode(loginDTO.senha()));
        login.setRole(Role.ROLE_USER);
        login.setUsuario(novoUsuario);
        novoUsuario.setLogin(login);

        // saveAndFlush: se outro cadastro simultâneo passou pelas checagens acima, a violação do unique estoura
        // aqui como DataIntegrityViolationException, que o ControllerAdviceHandler devolve como 409
        novoUsuario = usuarioRepository.saveAndFlush(novoUsuario);
        return UsuarioMapper.paraDTO(novoUsuario);
    }

    public UsuarioResponseDTO findByEmail(@NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findByEmail(emailAutenticado)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário autenticado não encontrado"));
        return UsuarioMapper.paraDTO(usuario);
    }

    /**
     * Permite corrigir o CPF apenas quando a verificação foi REJEITADA (ex.: erro de digitação). O status volta para
     * PENDENTE e o job verifica de novo.
     */
    @Transactional
    public UsuarioResponseDTO corrigirCpf(
            @NonNull Long id, @Valid CpfCorrecaoDTO dto, @NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
        if (!usuario.getEmail().equals(emailAutenticado)) {
            throw new ForbiddenException("Acesso negado: você não pode alterar dados de outro usuário");
        }
        if (usuario.getStatusVerificacaoCpf() != StatusVerificacaoCpf.REJEITADO) {
            throw new ConflictException("O CPF só pode ser corrigido quando a verificação foi rejeitada");
        }

        String cpf = CpfValidator.somenteDigitos(dto.cpf());
        if (!cpf.equals(usuario.getCpf()) && usuarioRepository.existsByCpf(cpf)) {
            throw new ConflictException(MensagensConflito.CPF);
        }

        usuario.setCpf(cpf);
        usuario.setStatusVerificacaoCpf(StatusVerificacaoCpf.PENDENTE);
        usuario.setTentativasVerificacaoCpf(0);
        usuario.setProximaVerificacaoCpfEm(null);
        return UsuarioMapper.paraDTO(usuarioRepository.saveAndFlush(usuario));
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::paraDTO).toList();
    }

    public UsuarioResponseDTO findByLogin(@NonNull String login, @NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findByLogin_Login(login)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o login: " + login));
        if (!usuario.getEmail().equals(emailAutenticado)) {
            throw new ForbiddenException("Acesso negado: você não pode visualizar dados de outro usuário");
        }
        return UsuarioMapper.paraDTO(usuario);
    }

    public UsuarioResponseDTO findById(@NonNull Long id, @NonNull String emailAutenticado) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado com o ID: " + id));
        if (!usuario.getEmail().equals(emailAutenticado)) {
            throw new ForbiddenException("Acesso negado: você não pode visualizar dados de outro usuário");
        }
        return UsuarioMapper.paraDTO(usuario);
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
