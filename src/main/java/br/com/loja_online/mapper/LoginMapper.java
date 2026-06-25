package br.com.loja_online.mapper;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.model.Login;

public class LoginMapper {
    public static LoginDTO paraDTO(Login login) {
        return new LoginDTO(login.getLogin(), null);
    }

    public static Login paraLogin(LoginDTO loginDTO) {
        return Login.builder().login(loginDTO.login()).senha(loginDTO.senha()).build();
    }
}
