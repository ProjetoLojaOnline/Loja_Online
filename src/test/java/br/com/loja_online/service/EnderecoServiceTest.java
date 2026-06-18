package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.EnderecoRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    private static final String PROPRIETARIO_EMAIL = "proprietario@example.com";

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EnderecoService enderecoService;

    private Endereco endereco;
    private Usuario proprietario;

    @BeforeEach
    void setUp() {
        proprietario = Usuario.builder().email(PROPRIETARIO_EMAIL).build();
        endereco = EnderecoBuilder.padrao().buildModel();
        endereco.setUsuario(proprietario);
    }

    @Test
    @DisplayName("criarEnderecoDeveRetornarEnderecoSalvo")
    void criarEnderecoDeveRetornarEnderecoSalvo() {
        when(usuarioRepository.findByEmail(PROPRIETARIO_EMAIL)).thenReturn(Optional.of(proprietario));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);

        Endereco resultado = enderecoService.criarEndereco(endereco, PROPRIETARIO_EMAIL);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getLogradouro()).isEqualTo(endereco.getLogradouro());
        verify(enderecoRepository).save(any(Endereco.class));
        verify(usuarioRepository).findByEmail(PROPRIETARIO_EMAIL);
    }

    @Test
    @DisplayName("findByIdDeveRetornarEnderecoQuandoExiste")
    void findByIdDeveRetornarEnderecoQuandoExiste() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        Endereco resultado = enderecoService.findById(1);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCep()).isEqualTo(endereco.getCep());
    }

    @Test
    @DisplayName("findByIdDeveLancarExceptionQuandoNaoExiste")
    void findByIdDeveLancarExceptionQuandoNaoExiste() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.findById(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteByIdDeveRemoverQuandoExiste")
    void deleteByIdDeveRemoverQuandoExiste() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        enderecoService.deleteById(1, PROPRIETARIO_EMAIL);

        verify(enderecoRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteByIdDeveLancarExceptionQuandoNaoExiste")
    void deleteByIdDeveLancarExceptionQuandoNaoExiste() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.deleteById(99, PROPRIETARIO_EMAIL))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
