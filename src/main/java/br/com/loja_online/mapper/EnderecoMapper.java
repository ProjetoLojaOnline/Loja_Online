package br.com.loja_online.mapper;

import org.springframework.lang.NonNull;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.model.Endereco;

public class EnderecoMapper {
    public static EnderecoDTO paraDto(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoDTO(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getComplemento(),
                endereco.getReferencia(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado());
    }

    public static @NonNull Endereco paraEndereco(@NonNull EnderecoDTO dto) {
        return new Endereco(
                dto.logradouro(),
                dto.numero(),
                dto.bairro(),
                dto.complemento(),
                dto.referencia(),
                dto.cep(),
                dto.cidade(),
                dto.estado());
    }
}
