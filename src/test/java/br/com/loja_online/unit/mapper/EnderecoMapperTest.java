package br.com.loja_online.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.mapper.EnderecoMapper;
import br.com.loja_online.model.Endereco;

@SuppressWarnings("null")
class EnderecoMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCampos")
    void paraDtoDeveMapearTodosCampos() {
        Endereco endereco = EnderecoBuilder.padrao().buildModel();

        EnderecoDTO dto = EnderecoMapper.paraDto(endereco);

        assertThat(dto.logradouro()).isEqualTo(endereco.getLogradouro());
        assertThat(dto.numero()).isEqualTo(endereco.getNumero());
        assertThat(dto.bairro()).isEqualTo(endereco.getBairro());
        assertThat(dto.cep()).isEqualTo(endereco.getCep());
        assertThat(dto.cidade()).isEqualTo(endereco.getCidade());
        assertThat(dto.estado()).isEqualTo(endereco.getEstado());
    }

    @Test
    @DisplayName("paraEnderecoDeveMapearTodosCampos")
    void paraEnderecoDeveMapearTodosCampos() {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();

        Endereco endereco = EnderecoMapper.paraEndereco(dto);

        assertThat(endereco.getLogradouro()).isEqualTo(dto.logradouro());
        assertThat(endereco.getNumero()).isEqualTo(dto.numero());
        assertThat(endereco.getCep()).isEqualTo(dto.cep());
    }
}
