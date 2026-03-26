package br.com.loja_online.service;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test") // Isso diz: "Use o application-test.properties"
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCriarUmUsuario() {
        //ARRANGE
        UsuarioRequestDTO usuarioDTO = new UsuarioRequestDTO();
        usuarioDTO.setNome("Alfred");
        usuarioDTO.setEmail("Alfred@teste.com");
        usuarioDTO.setTelefone("449999999");
        usuarioDTO.setCpf("3002434987456");

        LoginDTO loginDTO = new LoginDTO(1l, "Batman", "123456");

        UsuarioResponseDTO resultado = usuarioService.insert(usuarioDTO, loginDTO);

        assertNotNull(resultado);

        assertEquals("Alfred", resultado.getNome());

        assertTrue(usuarioRepository.existsByEmail("Alfred@teste.com"));

    }
    @Test
    void deveLancarErroSeLoginExistir() {
        UsuarioRequestDTO usuario1= new UsuarioRequestDTO();
        usuario1.setNome("Alfred");
        usuario1.setEmail("Alfred@teste.com");
        usuario1.setTelefone("449999999");
        usuario1.setCpf("3002434987456");

        LoginDTO login1 = new LoginDTO(1l, "Batman", "123456");

        usuarioService.insert(usuario1, login1);

        UsuarioRequestDTO usuario2 = new UsuarioRequestDTO();
        usuario2.setNome("Coringa");
        usuario2.setEmail("Coring@teste,com");
        usuario2.setTelefone("447777777");
        usuario2.setCpf("20034535986347");

        LoginDTO login2 = new LoginDTO(2l, "Batman", "123456");

        RuntimeException erro = assertThrows(RuntimeException.class, () -> {
            usuarioService.insert(usuario2, login2);

        });
        assertEquals("Este login já está em uso!",erro.getMessage());
    }
    @Test
    void deveLancarErroSeEmailJaExistir() {
        UsuarioRequestDTO usuario1 = new UsuarioRequestDTO();
        usuario1.setNome("Batman");
        usuario1.setEmail("Batman@teste.com");
        usuario1.setTelefone("339999999");
        usuario1.setCpf("3002434987456");

        LoginDTO login1 = new LoginDTO(1l, "Batman", "123456");

        usuarioService.insert(usuario1, login1);

        UsuarioRequestDTO usuario2 = new UsuarioRequestDTO();
        usuario2.setNome("Coringa");
        usuario2.setEmail("Batman@teste.com");
        usuario2.setTelefone("453333333");
        usuario2.setCpf("3002434987456");

        LoginDTO login2 = new LoginDTO(2l, "Coringa", "123456");

        RuntimeException erro = assertThrows(RuntimeException.class, () -> {
            usuarioService.insert(usuario2, login2);
        });
        assertEquals("Este e-mail já está cadastrado!", erro.getMessage());

    }
    @Test
    void nãoDeveCriarOutroUsuarioComMesmoCPF() {
        UsuarioRequestDTO usuario1 = new UsuarioRequestDTO();
        usuario1.setNome("Mazur");
        usuario1.setEmail("Mazur@teste.com");
        usuario1.setTelefone("5511111111");
        usuario1.setCpf("3002434987456");
        LoginDTO login1 = new LoginDTO(null, "mazur", "123456");

        usuarioService.insert(usuario1, login1);

        UsuarioRequestDTO usuario2 = new UsuarioRequestDTO();
        usuario2.setNome("Bueno");
        usuario2.setEmail("Bueno@teste.com");
        usuario2.setTelefone("55222222");
        usuario2.setCpf("3002434987456");
        LoginDTO login2 = new LoginDTO(null, "Batman", "123456");

        RuntimeException erro = assertThrows(RuntimeException.class, () -> {
            usuarioService.insert(usuario2, login2);
        });
        assertEquals("Este CPF já está cadastrado!", erro.getMessage());
    }
    @Test
    void deveMostrarErroSeCriarUsuarioComMesmoTelefone() {

        UsuarioRequestDTO usuarioDTO = new UsuarioRequestDTO();
        usuarioDTO.setNome("Alfred");
        usuarioDTO.setEmail("Alfred@teste.com");
        usuarioDTO.setTelefone("559999999");
        usuarioDTO.setCpf("3002434987456");
        LoginDTO login1 = new LoginDTO(null, "Batman", "123456");

        usuarioService.insert(usuarioDTO, login1);

        UsuarioRequestDTO usuarioDTO2= new UsuarioRequestDTO();
        usuarioDTO2.setNome("coringa");
        usuarioDTO2.setEmail("coringa@teste.com");
        usuarioDTO2.setTelefone("559999999");
        usuarioDTO2.setCpf("29932380741");
        LoginDTO login2 = new LoginDTO(null, "Coringa", "123456");

        RuntimeException erro = assertThrows(RuntimeException.class, () -> {
            usuarioService.insert(usuarioDTO2, login2);
        });
        assertEquals("Esse telefone já está em usso!", erro.getMessage());
    }
    @Test
    void deveCriptografarSenha() {

        UsuarioRequestDTO usuarioDTO = new UsuarioRequestDTO();
        usuarioDTO.setNome("Alfred");
        usuarioDTO.setEmail("Alfred@teste.com");
        usuarioDTO.setTelefone("559999999");
        usuarioDTO.setCpf("3002434987456");

        LoginDTO loginDTO = new LoginDTO(1l,"alfred", "123456");

        usuarioService.insert(usuarioDTO, loginDTO);

        Login loginsSalvo = loginRepository.findByLogin("alfred").get();

        assertTrue(passwordEncoder.matches("123456", loginsSalvo.getSenha()));

    }
    @Test
    void deveMostrarUsuarioNãoEncontradoComEsseLogin() {

        String login = "alfred";

        RuntimeException erro = assertThrows(ObjectNotFoundException.class, () -> {
            usuarioService.findByLogin(login);
        });
        assertEquals("Usuário não encontrado com o login: " + login, erro.getMessage());
    }
    @Test
    void deveMostrarUsuarioNãoEncotradoComEsseId() {

        Long id = 1l;

        assertThrows(ObjectNotFoundException.class, () -> {
            usuarioService.findById(id);
        });
    }
    @Test
    void deveMostrarUsuarioNãoEncontradoComEsseIdParaDeletar() {

        Long id = 1l;

        assertThrows(ObjectNotFoundException.class, () -> {
            usuarioService.deleteById(id);
        });
    }
    @Test
    void deDeletarUsuarioComId() {
        UsuarioRequestDTO usuarioDTO = new UsuarioRequestDTO();
        usuarioDTO.setNome("Mario");
        usuarioDTO.setEmail("Princesa@teste.com");
        usuarioDTO.setTelefone("44666666");
        usuarioDTO.setCpf("29932380741");

        LoginDTO login = new LoginDTO(1l, "Batman", "123456");

        UsuarioResponseDTO usuario = usuarioService.insert(usuarioDTO, login);
        Long iD = usuario.getId();

         usuarioService.deleteById(iD);

         assertThrows(ObjectNotFoundException.class, () -> {
             usuarioService.deleteById(iD);
             assertFalse(usuarioRepository.findById(iD).isPresent());
         });
    }

    @Test
    void UsuarioAtulizadoComSucesso() {

        UsuarioRequestDTO usuarioDTO= new UsuarioRequestDTO();
        usuarioDTO.setNome("Mazur");
        usuarioDTO.setEmail("mazur@teste.com");
        usuarioDTO.setCpf("12345678901");
        usuarioDTO.setTelefone("5511111111");
        LoginDTO login = new LoginDTO(null, "Batman", "123456");

        UsuarioResponseDTO resultado = usuarioService.insert(usuarioDTO, login);
        Long id = resultado.getId();

        UsuarioUpdateDTO usuarioDTO2= new UsuarioUpdateDTO(
                "limão",
                "441111111",
                "foto.png",
                "Masculino"
        );


        UsuarioResponseDTO responseDTO = usuarioService.atualizaUsuario(id, usuarioDTO2);

        assertEquals("limão", responseDTO.getNome());
        assertEquals("441111111", responseDTO.getTelefone());

        assertEquals("12345678901", responseDTO.getCpf());

    }
    @Test
    void deveMostrarNãoEncontradoComEsseIdParaAtualizar(){

        Long iDfak = 999L;

        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO(
          "batman",
          "441111111",
          "fot.png",
          "masculino"
        );

        RuntimeException erro = assertThrows(ObjectNotFoundException.class, () -> {
            usuarioService.atualizaUsuario(iDfak, updateDTO);
        });

        assertEquals("Usuario Não encontrado com o ID: " + iDfak, erro.getMessage());

    }

}
