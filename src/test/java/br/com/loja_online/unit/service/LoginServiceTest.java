package br.com.loja_online.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.security.TokenService;
import br.com.loja_online.service.LoginService;
import br.com.loja_online.service.exceptions.AuthenticationException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginService loginService;

    private Login login;

    @BeforeEach
    void setUp() {
        login = Login.builder().login("testuser").senha("encodedpassword").build();
    }

    // ── buscarPorLogin ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoBuscarPorLoginExistente")
    void deveRetornarLoginDTOQuandoBuscarPorLoginExistente() {
        String loginStr = "testuser";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.of(login));

        LoginDTO result = loginService.buscarPorLogin(loginStr);

        assertThat(result).isNotNull();
        assertThat(result.login()).isEqualTo("testuser");
        assertThat(result.senha()).isNull();
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoBuscarPorLoginInexistente")
    void deveLancarExcecaoQuandoBuscarPorLoginInexistente() {
        String loginStr = "nonexistent";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.buscarPorLogin(loginStr))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: nonexistent");
    }

    @Test
    @DisplayName("deveLidarComLoginNuloQuandoBuscarPorLoginComParametroNulo")
    void deveLidarComLoginNuloQuandoBuscarPorLoginComParametroNulo() {
        when(loginRepository.findByLogin(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.buscarPorLogin(null))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: null");
    }

    @Test
    @DisplayName("deveLidarComLoginVazioQuandoBuscarPorLoginComParametroVazio")
    void deveLidarComLoginVazioQuandoBuscarPorLoginComParametroVazio() {
        String loginStr = "";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.buscarPorLogin(loginStr))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: ");
    }

    // ── LoginMapper ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deveMapearLoginParaDTOSemExporSenha")
    void deveMapearLoginParaDTOSemExporSenha() {
        Login loginModel =
                Login.builder().login("usuario123").senha("hashSuperSecreto").build();

        LoginDTO result = LoginMapper.paraDTO(loginModel);

        assertThat(result.login()).isEqualTo("usuario123");
        assertThat(result.senha()).isNull();
    }

    @Test
    @DisplayName("deveMapearDTOParaLoginComLoginESenha")
    void deveMapearDTOParaLoginComLoginESenha() {
        LoginDTO loginDTO = new LoginDTO("usuario123", "senha123");

        Login result = LoginMapper.paraLogin(loginDTO);

        assertThat(result.getLogin()).isEqualTo("usuario123");
        assertThat(result.getSenha()).isEqualTo("senha123");
    }

    // ── login() por email ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deveAutenticarPorEmailQuandoIdentificadorForEmail")
    void deveAutenticarPorEmailQuandoIdentificadorForEmail() {
        String email = "user@example.com";
        String senha = "password123";
        String tokenEsperado = "header.payload.signature";
        Usuario usuario = Usuario.builder().email(email).login(login).build();
        login.setUsuario(usuario);

        when(loginRepository.findByLogin(email)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(senha, login.getSenha())).thenReturn(true);
        when(tokenService.gerarToken(email, "ROLE_USER")).thenReturn(tokenEsperado);

        String result = loginService.login(email, senha);

        assertThat(result).isEqualTo(tokenEsperado);
        verify(tokenService).gerarToken(email, "ROLE_USER");
    }

    // ── login() por username ──────────────────────────────────────────────────

    @Test
    @DisplayName("deveAutenticarPorUsernameQuandoIdentificadorForUsername")
    void deveAutenticarPorUsernameQuandoIdentificadorForUsername() {
        String username = "testuser";
        String email = "user@example.com";
        String senha = "password123";
        Usuario usuario = Usuario.builder().email(email).login(login).build();
        login.setUsuario(usuario);

        when(loginRepository.findByLogin(username)).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(senha, login.getSenha())).thenReturn(true);
        when(tokenService.gerarToken(email, "ROLE_USER")).thenReturn("token-username");

        String result = loginService.login(username, senha);

        assertThat(result).isEqualTo("token-username");
        verify(loginRepository).findByLogin(username);
        verify(tokenService).gerarToken(email, "ROLE_USER");
    }

    // ── login() erros ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deveLancarExcecaoQuandoIdentificadorNaoEncontrado")
    void deveLancarExcecaoQuandoIdentificadorNaoEncontrado() {
        String identificador = "nonexistent@example.com";
        String senha = "password123";
        when(loginRepository.findByLogin(identificador)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(identificador)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(identificador, senha))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoSenhaIncorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        String email = "user@example.com";
        String senha = "wrongpassword";
        Usuario usuario = Usuario.builder().email(email).login(login).build();
        login.setUsuario(usuario);

        when(loginRepository.findByLogin(email)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(senha, login.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(email, senha))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoLoginDoUsuarioNulo")
    void deveLancarExcecaoQuandoLoginDoUsuarioNulo() {
        String email = "user@example.com";
        String senha = "password123";
        Usuario usuario = Usuario.builder().email(email).login(null).build();

        when(loginRepository.findByLogin(email)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> loginService.login(email, senha))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoIdentificadorNulo")
    void deveLancarExcecaoQuandoIdentificadorNulo() {
        when(loginRepository.findByLogin(null)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(null, "password123"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoSenhaNula")
    void deveLancarExcecaoQuandoSenhaNula() {
        String email = "user@example.com";
        Usuario usuario = Usuario.builder().email(email).login(login).build();
        login.setUsuario(usuario);

        when(loginRepository.findByLogin(email)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(null, login.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(email, null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("deveChamarTokenServiceQuandoLoginBemSucedido")
    void deveChamarTokenServiceQuandoLoginBemSucedido() {
        String email = "user@example.com";
        String senha = "password123";
        Usuario usuario = Usuario.builder().email(email).login(login).build();
        login.setUsuario(usuario);

        when(loginRepository.findByLogin(email)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(senha, login.getSenha())).thenReturn(true);
        when(tokenService.gerarToken(email, "ROLE_USER")).thenReturn("token-mock");

        loginService.login(email, senha);

        verify(usuarioRepository).findByEmail(email);
        verify(passwordEncoder).matches(senha, login.getSenha());
        verify(tokenService).gerarToken(email, "ROLE_USER");
    }
}
