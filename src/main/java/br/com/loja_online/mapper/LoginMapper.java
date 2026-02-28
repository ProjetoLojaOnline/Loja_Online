package br.com.loja_online.mapper;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.Usuario;

public class LoginMapper {
    public static LoginDTO paraDTO(Login login) {
      return new LoginDTO(login.getId(), login.getLogin(), null);
    }


        public static Login paraLogin(LoginDTO loginDTO) {
            return Login.builder()
                    .login(loginDTO.login())
                    .senha(loginDTO.senha())
                    .build();

    }
}
