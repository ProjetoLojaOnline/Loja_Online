package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

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

}
