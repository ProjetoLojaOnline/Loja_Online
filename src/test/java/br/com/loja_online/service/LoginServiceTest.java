package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    LoginDTO dto;

    @Mock
    private LoginRepository repository;

    @InjectMocks
    private LoginService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginNãoEncotrado() {
        //Arrange
        String login = "não existente ";
        when(repository.findByLogin(login)).thenReturn(Optional.empty());

        //Assert + Act
        assertThrows(ObjectNotFoundException.class, () -> {

            service.buscarPorLogin(login);
        });

    }
    @Test
    void eveRetornarLoginDTOPorLoginComSucesso () {
        //ARRANGE
        String login = "Alfred";

        Login loginBanco = new Login();
        loginBanco.setLogin(login);
        loginBanco.setSenha("123456");
        when(repository.findByLogin(login)).thenReturn(Optional.of(loginBanco));

        //ACT
        LoginDTO resultado = service.buscarPorLogin(login);

        //ASSERT
        assertNotNull(resultado);
        assertEquals(login, resultado.login());
        verify(repository, times(1)).findByLogin(login);

    }
}