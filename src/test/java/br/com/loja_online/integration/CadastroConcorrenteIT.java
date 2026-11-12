package br.com.loja_online.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

/** Issue #140: cadastros simultâneos com o mesmo CPF devem resultar em 201 + 409, nunca 500. */
@SuppressWarnings("null")
class CadastroConcorrenteIT extends AbstractIntegrationTest {

    @MockitoSpyBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    @BeforeEach
    void setUp() {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private int cadastrar(UsuarioCadastroWrapper wrapper) throws Exception {
        return mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    @Test
    @DisplayName("deveRetornar201E409QuandoDoisCadastrosSimultaneosComMesmoCpf")
    void deveRetornar201E409QuandoDoisCadastrosSimultaneosComMesmoCpf() throws Exception {
        String cpf = UsuarioBuilder.novoCpfValido();
        List<UsuarioCadastroWrapper> cadastros = List.of(
                UsuarioBuilder.padrao().comCpf(cpf).buildWrapper(),
                UsuarioBuilder.padrao().comCpf(cpf).buildWrapper());

        ExecutorService pool = Executors.newFixedThreadPool(cadastros.size());
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Integer>> respostas = new ArrayList<>();
        try {
            for (UsuarioCadastroWrapper cadastro : cadastros) {
                Callable<Integer> requisicao = () -> {
                    largada.await();
                    return cadastrar(cadastro);
                };
                respostas.add(pool.submit(requisicao));
            }
            largada.countDown();

            List<Integer> status = new ArrayList<>();
            for (Future<Integer> resposta : respostas) {
                status.add(resposta.get(30, TimeUnit.SECONDS));
            }
            assertThat(status).containsExactlyInAnyOrder(201, 409);
        } finally {
            pool.shutdownNow();
        }
        assertThat(usuarioRepository.existsByCpf(cpf)).isTrue();
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("deveRetornar409ComMensagemClaraQuandoCpfDuplicadoPassaPelaChecagem")
    void deveRetornar409ComMensagemClaraQuandoCpfDuplicadoPassaPelaChecagem() throws Exception {
        // Simula, de forma determinística, o cadastro que perdeu a corrida: a checagem do serviço não viu o outro
        // registro, então quem barra é a constraint unique do banco
        String cpf = UsuarioBuilder.novoCpfValido();
        assertThat(cadastrar(UsuarioBuilder.padrao().comCpf(cpf).buildWrapper()))
                .isEqualTo(201);
        doReturn(false).when(usuarioRepository).existsByCpf(cpf);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UsuarioBuilder.padrao().comCpf(cpf).buildWrapper())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este CPF já está cadastrado!"));
    }

    @Test
    @DisplayName("deveTratarCpfComESemPontuacaoComoOMesmo")
    void deveTratarCpfComESemPontuacaoComoOMesmo() throws Exception {
        String cpf = UsuarioBuilder.novoCpfValido();
        String cpfFormatado =
                cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);

        assertThat(cadastrar(UsuarioBuilder.padrao().comCpf(cpfFormatado).buildWrapper()))
                .isEqualTo(201);
        assertThat(cadastrar(UsuarioBuilder.padrao().comCpf(cpf).buildWrapper()))
                .isEqualTo(409);
        assertThat(usuarioRepository.existsByCpf(cpf)).isTrue();
    }

    @Test
    @DisplayName("deveRetornarUsuarioAutenticadoEmMe")
    void deveRetornarUsuarioAutenticadoEmMe() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao();
        UsuarioCriado criado = criarUsuarioComToken(builder);

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + criado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.id()))
                .andExpect(jsonPath("$.email").value(builder.getEmail()));
    }
}
