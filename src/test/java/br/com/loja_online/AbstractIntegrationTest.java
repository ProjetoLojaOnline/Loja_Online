package br.com.loja_online;

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

import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.LoginRequest;
import br.com.loja_online.dto.UsuarioCadastroWrapper;

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

    public record UsuarioCriado(Long id, String token) {}

    protected UsuarioCriado criarUsuarioComToken(UsuarioBuilder builder) throws Exception {
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        LoginRequest loginRequest = new LoginRequest(builder.getEmail(), builder.getSenha());
        String token = mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new UsuarioCriado(id, token);
    }

    protected String criarUsuarioEObterToken() throws Exception {
        return criarUsuarioComToken(UsuarioBuilder.padrao()).token();
    }
}
