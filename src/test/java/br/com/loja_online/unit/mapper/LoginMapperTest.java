package br.com.loja_online.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.mapper.LoginMapper;
import br.com.loja_online.model.Login;

class LoginMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearLoginEOmitirSenha")
    void paraDtoDeveMapearLoginEOmitirSenha() {
        Login login = Login.builder().login("usuario123").senha("senha_hashed").build();

        LoginDTO dto = LoginMapper.paraDTO(login);

        assertThat(dto.login()).isEqualTo("usuario123");
        assertThat(dto.senha()).isNull();
    }

    @Test
    @DisplayName("paraLoginDeveMapearLoginESenha")
    void paraLoginDeveMapearLoginESenha() {
        LoginDTO dto = new LoginDTO("usuario123", "senha456");

        Login login = LoginMapper.paraLogin(dto);

        assertThat(login.getLogin()).isEqualTo("usuario123");
        assertThat(login.getSenha()).isEqualTo("senha456");
    }
}
