package br.com.loja_online.security;

import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private Usuario usuario;
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", "minha-chave-secreta-teste");

        // Monta o Login com o login do usuário
        Login login = Login.builder()
                .login("testuser")
                .senha("encodedpassword")
                .build();

        // Monta o Usuario com o Login dentro
        this.usuario = Usuario.builder()
                .id(1L)
                .nome("Test User")
                .login(login)
                .build();
    }

    @Test
    @DisplayName("deveRetornarTokenNaoNulo")
    void deveRetornarTokenNaoNulo() {
        String token = tokenService.gerarToken(usuario.getLogin());

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }
    @Test
    @DisplayName("deveRetornarTokenComSubjectCorreto")
    void deveRetornarTokenComSubjectCorreto() {
        String token = tokenService.gerarToken(usuario.getLogin());
        String subject = tokenService.getSubject(token);

        assertThat(subject).isEqualTo("testuser");
    }
    @Test
    @DisplayName("deveRetornarSubjectDoTokenGerado")
    void deveRetornarSubjectDoTokenGerado() {
        String token = tokenService.gerarToken(usuario.getLogin());

        String subject = tokenService.getSubject(token);

        assertThat(subject).isNotNull();
        assertThat(subject).isEqualTo(usuario.getLogin().getLogin());
    }
    @Test
    @DisplayName("deveLancarExcecaoQuandoTokenInvalido")
    void deveLancarExcecaoQuandoTokenInvalido() {
        assertThatThrownBy(() -> tokenService.getSubject("token.invalido.aqui"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Toke JWT inválido ou exprirado!");
    }
}
