package br.com.loja_online.service;

import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticacaoServiceTest {

    @Mock
    private LoginRepository loginRepository; // ← dependência do service

    @InjectMocks
    private AuthenticacaoService autenticacaoService;

    private Login login;

    @BeforeEach
    void setUp() {
        login = Login.builder()
                .login("testuser")
                .senha("encodedpassword")
                .build();
    }

    @Test
    @DisplayName("deveRetornarUserDetailsQuandoLoginExistente")
    void deveRetornarUserDetailsQuandoLoginExistente() {
        // Given
        when(loginRepository.findByLogin("testuser")).thenReturn(login);

        // When
        UserDetails result = autenticacaoService.loadUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("deveLancarExcecaoQuandoLoginNaoExistente")
    void deveLancarExcecaoQuandoLoginNaoExistente() {
        // Given
        when(loginRepository.findByLogin("nonexistent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> autenticacaoService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Login não encontrado: nonexistent");
    }
}