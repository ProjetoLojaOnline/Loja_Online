package br.com.loja_online.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.mapper.UsuarioMapper;
import br.com.loja_online.mapper.UsuarioUpdateMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Usuario;

@SuppressWarnings("null")
class UsuarioMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCampos")
    void paraDtoDeveMapearTodosCampos() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .cpf("12345678900")
                .telefone("11999999999")
                .build();

        UsuarioResponseDTO dto = UsuarioMapper.paraDTO(usuario);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNome()).isEqualTo("João Silva");
        assertThat(dto.getEmail()).isEqualTo("joao@email.com");
        assertThat(dto.getCpf()).isEqualTo("12345678900");
        assertThat(dto.getTelefone()).isEqualTo("11999999999");
        assertThat(dto.getEnderecos()).isEmpty();
    }

    @Test
    @DisplayName("paraDtoDeveRetornarNullQuandoUsuarioNull")
    void paraDtoDeveRetornarNullQuandoUsuarioNull() {
        UsuarioResponseDTO dto = UsuarioMapper.paraDTO(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("paraDtoDeveMapearEnderecosQuandoPresentes")
    void paraDtoDeveMapearEnderecosQuandoPresentes() {
        Endereco endereco = EnderecoBuilder.padrao().buildModel();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("João")
                .email("joao@email.com")
                .enderecos(List.of(endereco))
                .build();

        UsuarioResponseDTO dto = UsuarioMapper.paraDTO(usuario);

        assertThat(dto.getEnderecos()).hasSize(1);
        assertThat(dto.getEnderecos().get(0).cep()).isEqualTo(endereco.getCep());
    }

    @Test
    @DisplayName("paraUsuarioDeveMapearTodosCampos")
    void paraUsuarioDeveMapearTodosCampos() {
        UsuarioRequestDTO requestDTO = UsuarioRequestDTO.builder()
                .nome("Maria Souza")
                .email("maria@email.com")
                .cpf("98765432100")
                .telefone("11988888888")
                .build();

        Usuario usuario = UsuarioMapper.paraUsuario(requestDTO);

        assertThat(usuario.getNome()).isEqualTo("Maria Souza");
        assertThat(usuario.getEmail()).isEqualTo("maria@email.com");
        assertThat(usuario.getCpf()).isEqualTo("98765432100");
        assertThat(usuario.getTelefone()).isEqualTo("11988888888");
    }

    @Test
    @DisplayName("paraUsuarioDeveRetornarNullQuandoDtoNull")
    void paraUsuarioDeveRetornarNullQuandoDtoNull() {
        Usuario usuario = UsuarioMapper.paraUsuario(null);

        assertThat(usuario).isNull();
    }

    @Test
    @DisplayName("updateUsuarioDtoDeveAtualizarCamposPermitidos")
    void updateUsuarioDtoDeveAtualizarCamposPermitidos() {
        Usuario usuario =
                Usuario.builder().nome("Nome Antigo").telefone("11900000000").build();
        UsuarioUpdateDTO dto = UsuarioUpdateDTO.builder()
                .nome("Nome Novo")
                .telefone("11911111111")
                .foto("foto.jpg")
                .genero("M")
                .build();

        UsuarioUpdateMapper.updateUsuarioDTO(dto, usuario);

        assertThat(usuario.getNome()).isEqualTo("Nome Novo");
        assertThat(usuario.getTelefone()).isEqualTo("11911111111");
        assertThat(usuario.getFoto()).isEqualTo("foto.jpg");
        assertThat(usuario.getGenero()).isEqualTo("M");
    }

    @Test
    @DisplayName("updateUsuarioDtoNaoDeveAlterarQuandoDtoNull")
    void updateUsuarioDtoNaoDeveAlterarQuandoDtoNull() {
        Usuario usuario = Usuario.builder().nome("Nome Original").build();

        UsuarioUpdateMapper.updateUsuarioDTO(null, usuario);

        assertThat(usuario.getNome()).isEqualTo("Nome Original");
    }
}
