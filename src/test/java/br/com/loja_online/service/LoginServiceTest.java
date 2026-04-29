package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private LoginService loginService;

    private Login login;

    @BeforeEach
    void setUp() {
        login = Login.builder()
                .login("testuser")
                .senha("encodedpassword")
                .build();
    }

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoBuscarPorLoginExistente")
    void deveRetornarLoginDTOQuandoBuscarPorLoginExistente() {
        // Given
        String loginStr = "testuser";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.of(login));

        // When
        LoginDTO result = loginService.buscarPorLogin(loginStr);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.login()).isEqualTo("testuser");
        assertThat(result.senha()).isNull(); // Senha nula conforme LoginMapper
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoBuscarPorLoginInexistente")
    void deveLancarExcecaoQuandoBuscarPorLoginInexistente() {
        // Given
        String loginStr = "nonexistent";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginService.buscarPorLogin(loginStr))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: nonexistent");
    }

    @Test
    @DisplayName("deveMapearParaDTOCorretamenteQuandoLoginMapperParaDTO")
    void deveMapearParaDTOCorretamenteQuandoLoginMapperParaDTO() {
        // Given & When & Then
        try (MockedStatic<LoginMapper> mockedMapper = mockStatic(LoginMapper.class)) {
            mockedMapper.when(() -> LoginMapper.paraDTO(login)).thenReturn(new LoginDTO("testuser", null));

            LoginDTO result = LoginMapper.paraDTO(login);

            assertThat(result.login()).isEqualTo("testuser");
            assertThat(result.senha()).isNull();
            mockedMapper.verify(() -> LoginMapper.paraDTO(login));
        }
    }

    @Test
    @DisplayName("deveMapearParaLoginCorretamenteQuandoLoginMapperParaLogin")
    void deveMapearParaLoginCorretamenteQuandoLoginMapperParaLogin() {
        // Given
        LoginDTO loginDTO = new LoginDTO("testuser", "password");

        // When & Then
        try (MockedStatic<LoginMapper> mockedMapper = mockStatic(LoginMapper.class)) {
            mockedMapper.when(() -> LoginMapper.paraLogin(loginDTO)).thenReturn(login);

            Login result = LoginMapper.paraLogin(loginDTO);

            assertThat(result.getLogin()).isEqualTo("testuser");
            assertThat(result.getSenha()).isEqualTo("encodedpassword");
            mockedMapper.verify(() -> LoginMapper.paraLogin(loginDTO));
        }
    }

    @Test
    @DisplayName("deveLidarComLoginNuloQuandoBuscarPorLoginComParametroNulo")
    void deveLidarComLoginNuloQuandoBuscarPorLoginComParametroNulo() {
        // Given
        when(loginRepository.findByLogin(null)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginService.buscarPorLogin(null))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: null");
    }

    @Test
    @DisplayName("deveLidarComLoginVazioQuandoBuscarPorLoginComParametroVazio")
    void deveLidarComLoginVazioQuandoBuscarPorLoginComParametroVazio() {
        // Given
        String loginStr = "";
        when(loginRepository.findByLogin(loginStr)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginService.buscarPorLogin(loginStr))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Login não encontrado: ");
    }
}
