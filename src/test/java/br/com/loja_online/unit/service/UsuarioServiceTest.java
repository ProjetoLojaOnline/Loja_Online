package br.com.loja_online.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.mapper.UsuarioUpadateMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.UsuarioService;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String EMAIL_AUTENTICADO = "joao@example.com";

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
                .email(EMAIL_AUTENTICADO)
                .cpf("12345678901")
                .telefone("11999999999")
                .enderecos(Collections.emptyList())
                .build();

        loginDTO = new LoginDTO("joao", "senha123");

        usuario = Usuario.builder()
                .id(1L)
                .nome("João")
                .email(EMAIL_AUTENTICADO)
                .cpf("12345678901")
                .telefone("11999999999")
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
    @DisplayName("deveInserirUsuarioComSucessoQuandoDadosValidos")
    void deveInserirUsuarioComSucessoQuandoDadosValidos() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.insert(usuarioRequestDTO, loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("João");
        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(any(Usuario.class));
        assertThat(usuario.getLogin().getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoLoginJaEmUso")
    void deveLancarExcecaoQuandoLoginJaEmUso() {
        when(loginRepository.existsByLogin("joao")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este login já está em uso!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoEmailJaCadastrado")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(EMAIL_AUTENTICADO)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este e-mail já está cadastrado!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoCpfJaCadastrado")
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este CPF já está cadastrado!");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoTelefoneJaEmUso")
    void deveLancarExcecaoQuandoTelefoneJaEmUso() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone("11999999999")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.insert(usuarioRequestDTO, loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Esse telefone já está em uso!");
    }

    @Test
    @DisplayName("deveRetornarUsuarioQuandoFindByIdExistente")
    void deveRetornarUsuarioQuandoFindByIdExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO result = usuarioService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoFindByIdInexistente")
    void deveLancarExcecaoQuandoFindByIdInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(1L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado com o ID: 1");
    }

    @Test
    @DisplayName("deveDeletarUsuarioQuandoDeleteByIdExistente")
    void deveDeletarUsuarioQuandoDeleteByIdExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.deleteById(1L, EMAIL_AUTENTICADO);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoDeleteByIdInexistente")
    void deveLancarExcecaoQuandoDeleteByIdInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deleteById(1L, EMAIL_AUTENTICADO))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado para deletar");
    }

    @Test
    @DisplayName("deveAtualizarUsuarioQuandoAtualizaUsuarioComDadosParciais")
    void deveAtualizarUsuarioQuandoAtualizaUsuarioComDadosParciais() {
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("João Atualizado")
                .telefone("11888888888")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO);

        assertThat(result).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoAtualizaUsuarioInexistente")
    void deveLancarExcecaoQuandoAtualizaUsuarioInexistente() {
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder().nome("Teste").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Usuário não encontrado com o ID: 1");
    }

    @Test
    @DisplayName("deveMapearTodosOsCamposQuandoUsuarioMapperParaDTO")
    void deveMapearTodosOsCamposQuandoUsuarioMapperParaDTO() {
        Endereco endereco = new Endereco("Rua A", 1, "Centro", null, null, "01001-000", "São Paulo", "SP");
        Usuario usuarioCompleto = Usuario.builder()
                .id(1L)
                .nome("João")
                .email(EMAIL_AUTENTICADO)
                .cpf("12345678901")
                .telefone("11999999999")
                .genero("M")
                .tipo("CLIENTE")
                .enderecos(java.util.List.of(endereco))
                .build();

        UsuarioResponseDTO result = UsuarioMapper.paraDTO(usuarioCompleto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("João");
        assertThat(result.getEmail()).isEqualTo(EMAIL_AUTENTICADO);
        assertThat(result.getCpf()).isEqualTo("12345678901");
        assertThat(result.getTelefone()).isEqualTo("11999999999");
        assertThat(result.getGenero()).isEqualTo("M");
        assertThat(result.getTipo()).isEqualTo("CLIENTE");
        assertThat(result.getEnderecos()).hasSize(1);
    }

    @Test
    @DisplayName("deveRetornarNullQuandoUsuarioMapperParaDTOComNull")
    void deveRetornarNullQuandoUsuarioMapperParaDTOComNull() {
        assertThat(UsuarioMapper.paraDTO(null)).isNull();
    }

    @Test
    @DisplayName("deveMapearTodosOsCamposQuandoUsuarioMapperParaUsuario")
    void deveMapearTodosOsCamposQuandoUsuarioMapperParaUsuario() {
        Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("João");
        assertThat(result.getEmail()).isEqualTo(EMAIL_AUTENTICADO);
        assertThat(result.getCpf()).isEqualTo("12345678901");
        assertThat(result.getTelefone()).isEqualTo("11999999999");
    }

    @Test
    @DisplayName("deveRetornarNullQuandoUsuarioMapperParaUsuarioComNull")
    void deveRetornarNullQuandoUsuarioMapperParaUsuarioComNull() {
        assertThat(UsuarioMapper.paraUsuario(null)).isNull();
    }

    @Test
    @DisplayName("deveAtualizarNomeETelefoneQuandoUpdateUsuarioDTO")
    void deveAtualizarNomeETelefoneQuandoUpdateUsuarioDTO() {
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("Novo Nome")
                .telefone("11777777777")
                .build();

        UsuarioUpadateMapper.updateUsuarioDTO(updateDTO, usuario);

        assertThat(usuario.getNome()).isEqualTo("Novo Nome");
        assertThat(usuario.getTelefone()).isEqualTo("11777777777");
        assertThat(usuario.getEmail()).isEqualTo(EMAIL_AUTENTICADO);
    }

    @Test
    @DisplayName("deveManterDadosExistentesQuandoAtualizacaoParcial")
    void deveManterDadosExistentesQuandoAtualizacaoParcial() {
        UsuarioUpdateDTO updateDTO =
                UsuarioUpdateDTO.builder().nome("Novo Nome").build();

        UsuarioUpadateMapper.updateUsuarioDTO(updateDTO, usuario);

        assertThat(usuario.getNome()).isEqualTo("Novo Nome");
        assertThat(usuario.getEmail()).isEqualTo(EMAIL_AUTENTICADO);
    }

    @Test
    @DisplayName("deveLidarComListaVaziaQuandoUsuarioComEnderecosVazios")
    void deveLidarComListaVaziaQuandoUsuarioComEnderecosVazios() {
        usuarioRequestDTO.setEnderecos(Collections.emptyList());

        Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

        assertThat(result.getEnderecos()).isEmpty();
    }

    @Test
    @DisplayName("deveLidarComListaNulaQuandoUsuarioComEnderecosNulos")
    void deveLidarComListaNulaQuandoUsuarioComEnderecosNulos() {
        usuarioRequestDTO.setEnderecos(null);

        Usuario result = UsuarioMapper.paraUsuario(usuarioRequestDTO);

        assertThat(result.getEnderecos()).isNullOrEmpty();
    }

    @Test
    @DisplayName("deveCriptografarSenhaQuandoInserirUsuario")
    void deveCriptografarSenhaQuandoInserirUsuario() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.insert(usuarioRequestDTO, loginDTO);

        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("deveVerificarExistenciaLoginAntesDeInserir")
    void deveVerificarExistenciaLoginAntesDeInserir() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.insert(usuarioRequestDTO, loginDTO);

        verify(loginRepository).existsByLogin("joao");
    }

    @Test
    @DisplayName("deveVerificarExistenciaEmailAntesDeInserir")
    void deveVerificarExistenciaEmailAntesDeInserir() {
        when(loginRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByTelefone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.insert(usuarioRequestDTO, loginDTO);

        verify(usuarioRepository).existsByEmail(EMAIL_AUTENTICADO);
    }

    @Test
    @DisplayName("deveChamarFindByIdQuandoAtualizar")
    void deveChamarFindByIdQuandoAtualizar() {
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome("João Atualizado")
                .telefone("11999999999")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO);

        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("deveChamarDeleteQuandoDeleteExistente")
    void deveChamarDeleteQuandoDeleteExistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.deleteById(1L, EMAIL_AUTENTICADO);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deveAtualizarApenasNomeQuandoUpdateDTOTiverApenasNome")
    void deveAtualizarApenasNomeQuandoUpdateDTOTiverApenasNome() {
        UsuarioUpdateDTO updateDTO =
                UsuarioUpdateDTO.builder().nome("Nome Novo").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO);

        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveAtualizarApenasTelefoneQuandoUpdateDTOTiverApenasTelefone")
    void deveAtualizarApenasTelefoneQuandoUpdateDTOTiverApenasTelefone() {
        UsuarioUpdateDTO updateDTO =
                UsuarioUpdateDTO.builder().telefone("11988888777").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO);

        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("deveManterNomeOriginalQuandoNaoFornecido")
    void deveManterNomeOriginalQuandoNaoFornecido() {
        String nomeOriginal = usuario.getNome();
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                .nome(nomeOriginal)
                .telefone("11988888777")
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.atualizaUsuario(1L, updateDTO, EMAIL_AUTENTICADO);

        verify(usuarioRepository).save(usuario);
    }
}
