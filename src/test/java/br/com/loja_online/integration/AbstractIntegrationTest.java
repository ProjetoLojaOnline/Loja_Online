package br.com.loja_online.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.TestcontainersConfiguration;
import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.AutenticacaoRequestDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.model.Login;
import br.com.loja_online.model.enums.Role;
import br.com.loja_online.repository.LoginRepository;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private LoginRepository loginRepository;

    public record UsuarioCriado(Long id, String token) {}

    protected UsuarioCriado criarUsuarioComToken(UsuarioBuilder builder) throws Exception {
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        AutenticacaoRequestDTO loginRequest = new AutenticacaoRequestDTO(builder.getEmail(), null, builder.getSenha());
        String token = mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new UsuarioCriado(id, token);
    }

    protected String criarUsuarioEObterToken() throws Exception {
        return criarUsuarioComToken(UsuarioBuilder.padrao()).token();
    }

    protected String criarVendedorEObterToken() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao();
        criarUsuarioComToken(builder);

        Login login = loginRepository
                .findByLogin(builder.getLogin())
                .orElseThrow(() -> new IllegalStateException("Login não encontrado após criação"));
        login.setRole(Role.ROLE_VENDEDOR);
        loginRepository.save(login);

        AutenticacaoRequestDTO loginRequest = new AutenticacaoRequestDTO(builder.getEmail(), null, builder.getSenha());
        return mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
