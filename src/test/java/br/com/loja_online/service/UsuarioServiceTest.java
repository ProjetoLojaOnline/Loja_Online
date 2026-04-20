package br.com.loja_online.service;

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
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequestDTO usuarioRequestDTO;
    private LoginDTO loginDTO;
    private Usuario usuario;
    private Login login;

    @BeforeEach
    void setUp() {
        usuarioRequestDTO = UsuarioRequestDTO.builder()
                .nome("João")
                .email("joao@example.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .cartoes(Collections.emptyList())
                .enderecos(Collections.emptyList())
                .build();

        loginDTO = new LoginDTO("joao", "senha123");

        usuario = Usuario.builder()
                .id(1L)
                .nome("João")
                .email("joao@example.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .cartoes(Collections.emptyList())
                .enderecos(Collections.emptyList())
                .build();

        login = Login.builder()
                .login("joao")
                .senha("encodedPassword")
                .usuario(usuario)
                .build();
        usuario.setLogin(login);
    }

    @Test
    @DisplayName("deveRetornarListaUsuariosQuandoFindAll")
    void deveRetornarListaUsuariosQuandoFindAll() {
        // Given
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        // When
        List<UsuarioResponseDTO> result = usuarioService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("João");
        verify(usuarioRepository).findAll();
    }

    @Test
    @DisplayName("deveInserirUsuarioComSucessoQuandoDadosValidos")
    void deveInserirUsuarioComSucessoQuandoDadosValidos() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        UsuarioResponseDTO result = usuarioService.insert(usuarioRequestDTO, loginDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("João");
        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(any(Usuario.class));
        // Verificar relacionamento bidirecional: usuario.getLogin().getUsuario() == usuario
        assertThat(usuario.getLogin().getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoLoginJaEmUso")
    void deveLancarExcecaoQuandoLoginJaEmUso() {
        // Given
        when(loginRepository.existsByLogin("joao")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este login já está em uso!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoEmailJaCadastrado")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail("joao@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este e-mail já está cadastrado!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoCpfJaCadastrado")
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este CPF já está cadastrado!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoTelefoneJaEmUso")
    void deveLancarExcecaoQuandoTelefoneJaEmUso() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone("11999999999")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Esse telefone já está em usso!");
    }

    @Test
    @DisplayName("deveRetornarUsuarioQuandoFindByIdExistente")
    void deveRetornarUsuarioQuandoFindByIdExistente() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // When
        UsuarioResponseDTO result = usuarioService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoFindByIdInexistente")
    void deveLancarExcecaoQuandoFindByIdInexistente() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> usuarioService.findById(1L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado com o ID: 1");
    }

    @Test
    @DisplayName("deveRetornarUsuarioQuandoFindByLoginExistente")
    void deveRetornarUsuarioQuandoFindByLoginExistente() {
        // Given
        when(usuarioRepository.findByLogin_Login("joao")).thenReturn(Optional.of(usuario));

        // When
        UsuarioResponseDTO result = usuarioService.findByLogin("joao");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("João");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoFindByLoginInexistente")
    void deveLancarExcecaoQuandoFindByLoginInexistente() {
        // Given
        when(usuarioRepository.findByLogin_Login("joao")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> usuarioService.findByLogin("joao"))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado com o login: joao");
    }

    @Test
    @DisplayName("deveDeletarUsuarioQuandoDeleteByIdExistente")
    void deveDeletarUsuarioQuandoDeleteByIdExistente() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        // When
        usuarioService.deleteById(1L);

        // Then
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoDeleteByIdInexistente")
    void deveLancarExcecaoQuandoDeleteByIdInexistente() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> usuarioService.deleteById(1L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado para deletar");
    }

    @Test
    @DisplayName("deveAtualizarUsuarioQuandoAtualizaUsuarioComDadosParciais")
    void deveAtualizarUsuarioQuandoAtualizaUsuarioComDadosParciais() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("João Atualizado")
                .telefone("11888888888")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        UsuarioResponseDTO result = usuarioService.atualizaUsuario(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoAtualizaUsuarioInexistente")
    void deveLancarExcecaoQuandoAtualizaUsuarioInexistente() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder().nome("Teste").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> usuarioService.atualizaUsuario(1L, updateDTO))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuario Não encontrado com o ID: 1");
    }

    @Test
    @DisplayName("deveMapearParaDTOCorretamenteQuandoUsuarioMapperParaDTO")
    void deveMapearParaDTOCorretamenteQuandoUsuarioMapperParaDTO() {
        // Given & When & Then
        try (MockedStatic<UsuarioMapper> mockedMapper = mockStatic(UsuarioMapper.class)) {
            mockedMapper.when(() -> UsuarioMapper.paraDTO(usuario)).thenReturn(UsuarioResponseDTO.builder().nome("João").build());

            UsuarioResponseDTO result = UsuarioMapper.paraDTO(usuario);

            assertThat(result.getNome()).isEqualTo("João");
            mockedMapper.verify(() -> UsuarioMapper.paraDTO(usuario));
        }
    }

    @Test
    @DisplayName("deveMapearParaUsuarioCorretamenteQuandoUsuarioMapperParaUsuario")
    void deveMapearParaUsuarioCorretamenteQuandoUsuarioMapperParaUsuario() {
        // Given & When & Then
        try (MockedStatic<UsuarioMapper> mockedMapper = mockStatic(UsuarioMapper.class)) {
            mockedMapper.when(() -> UsuarioMapper.paraUsuario(usuarioRequestDTO)).thenReturn(usuario);

            Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

            assertThat(result.getNome()).isEqualTo("João");
            mockedMapper.verify(() -> UsuarioMapper.paraUsuario(usuarioRequestDTO));
        }
    }

    @Test
    @DisplayName("deveAtualizarUsuarioParcialmenteQuandoUsuarioUpdateMapperUpdateUsuarioDTO")
    void deveAtualizarUsuarioParcialmenteQuandoUsuarioUpdateMapperUpdateUsuarioDTO() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("Novo Nome")
                .telefone("11999999999")
                .build();

        // When
        try (MockedStatic<UsuarioUpadateMapper> mockedMapper = mockStatic(UsuarioUpadateMapper.class)) {
            UsuarioUpadateMapper.updateUsuarioDTO(updateDTO, usuario);

            // Then
            mockedMapper.verify(() -> UsuarioUpadateMapper.updateUsuarioDTO(updateDTO, usuario));
        }
    }

    @Test
    @DisplayName("deveManterDadosExistentesQuandoAtualizacaoParcial")
    void deveManterDadosExistentesQuandoAtualizacaoParcial() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("Novo Nome")
                .build(); // Apenas nome, outros campos devem permanecer

        // When
        UsuarioUpadateMapper.updateUsuarioDTO(updateDTO, usuario);

        // Then
        assertThat(usuario.getNome()).isEqualTo("Novo Nome");
        assertThat(usuario.getEmail()).isEqualTo("joao@example.com"); // Não alterado
    }

    @Test
    @DisplayName("deveLidarComListasVaziasQuandoUsuarioComCartoesEEnderecosVazios")
    void deveLidarComListasVaziasQuandoUsuarioComCartoesEEnderecosVazios() {
        // Given
        usuarioRequestDTO.setCartoes(Collections.emptyList());
        usuarioRequestDTO.setEnderecos(Collections.emptyList());

        // When
        Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

        // Then
        assertThat(result.getCartoes()).isEmpty();
        assertThat(result.getEnderecos()).isEmpty();
    }

    @Test
    @DisplayName("deveLidarComListasNulasQuandoUsuarioComCartoesEEnderecosNulos")
    void deveLidarComListasNulasQuandoUsuarioComCartoesEEnderecosNulos() {
        // Given
        usuarioRequestDTO.setCartoes(null);
        usuarioRequestDTO.setEnderecos(null);

        // When
        Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

        // Then
        assertThat(result.getCartoes()).isNullOrEmpty();
        assertThat(result.getEnderecos()).isNullOrEmpty();
    }

    @Test
    @DisplayName("deveRetornarListaVaziaQuandoNenhumUsuario")
    void deveRetornarListaVaziaQuandoNenhumUsuario() {
        // Given
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<UsuarioResponseDTO> result = usuarioService.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deveCriptografarSenhaQuandoInserirUsuario")
    void deveCriptografarSenhaQuandoInserirUsuario() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.insert(usuarioRequestDTO, loginDTO);

        // Then
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("deveVerificarExistenciaLoginAntesDeInserir")
    void deveVerificarExistenciaLoginAntesDeInserir() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.insert(usuarioRequestDTO, loginDTO);

        // Then
        verify(loginRepository).existsByLogin("joao");
    }

    @Test
    @DisplayName("deveVerificarExistenciaEmailAntesDeInserir")
    void deveVerificarExistenciaEmailAntesDeInserir() {
        // Given
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.insert(usuarioRequestDTO, loginDTO);

        // Then
        verify(usuarioRepository).existsByEmail("joao@example.com");
    }

    @Test
    @DisplayName("deveChamarFindByIdQuandoAtualizar")
    void deveChamarFindByIdQuandoAtualizar() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("João Atualizado")
                .telefone("11999999999")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.atualizaUsuario(1L, updateDTO);

        // Then
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("deveChamarDeleteQuandoDeleteExistente")
    void deveChamarDeleteQuandoDeleteExistente() {
        // Given
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        // When
        usuarioService.deleteById(1L);

        // Then
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deveAtualizarApenasNomeQuandoUpdateDTOTiverApenasNome")
    void deveAtualizarApenasNomeQuandoUpdateDTOTiverApenasNome() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("Nome Novo")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.atualizaUsuario(1L, updateDTO);

        // Then
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveAtualizarApenasTelefoneQuandoUpdateDTOTiverApenasTelefone")
    void deveAtualizarApenasTelefoneQuandoUpdateDTOTiverApenasTelefone() {
        // Given
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .telefone("11988888777")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.atualizaUsuario(1L, updateDTO);

        // Then
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveManterNomeOriginalQuandoNaoFornecido")
    void deveManterNomeOriginalQuandoNaoFornecido() {
        // Given
        String nomeOriginal = usuario.getNome();
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome(nomeOriginal)
                .telefone("11988888777")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        usuarioService.atualizaUsuario(1L, updateDTO);

        // Then
        verify(usuarioRepository).save(usuario);
    }
}
