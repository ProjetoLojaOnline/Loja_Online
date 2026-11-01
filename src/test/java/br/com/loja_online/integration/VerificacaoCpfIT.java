package br.com.loja_online.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.client.VerificadorCpfClient;
import br.com.loja_online.config.VerificacaoCpfProperties;
import br.com.loja_online.dto.CpfCorrecaoDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.model.enums.StatusVerificacaoCpf;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.VerificacaoCpfJob;
import br.com.loja_online.service.VerificacaoCpfService;

/** Critérios de aceite da issue #140: verificação assíncrona de CPF e cadastros simultâneos. */
@SuppressWarnings("null")
class VerificacaoCpfIT extends AbstractIntegrationTest {

    @MockitoBean
    private VerificadorCpfClient verificadorCpfClient;

    @MockitoSpyBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private VerificacaoCpfService verificacaoCpfService;

    @Autowired
    private VerificacaoCpfProperties verificacaoCpfProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow();
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
    @DisplayName("deveCarregarVerificacaoCpfPropertiesComOverridesDoPerfilDeTeste")
    void deveCarregarVerificacaoCpfPropertiesComOverridesDoPerfilDeTeste() {
        // Só existe no verificacao-cpf.properties (sem default no código): prova que o import foi carregado
        assertThat(applicationContext.getEnvironment().getProperty("verificacao.cpf.job.intervalo"))
                .isEqualTo("PT30S");
        // Overrides do application-test.properties precisam prevalecer sobre o arquivo importado
        assertThat(verificacaoCpfProperties.fake().rejeitados()).isEmpty();
        assertThat(applicationContext.getBeanProvider(VerificacaoCpfJob.class).getIfAvailable())
                .isNull();
    }

    @Test
    @DisplayName("deveCadastrarComoPendenteSemChamarOProvedorExterno")
    void deveCadastrarComoPendenteSemChamarOProvedorExterno() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UsuarioBuilder.padrao().buildWrapper())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusVerificacaoCpf").value("PENDENTE"));

        verifyNoInteractions(verificadorCpfClient);
    }

    @Test
    @DisplayName("deveAprovarNoJobEUsuarioConsultarOProprioStatus")
    void deveAprovarNoJobEUsuarioConsultarOProprioStatus() throws Exception {
        UsuarioCriado criado = criarUsuarioComToken(UsuarioBuilder.padrao());
        when(verificadorCpfClient.cpfExiste(anyString())).thenReturn(true);

        verificacaoCpfService.processarPendentes();

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + criado.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.id()))
                .andExpect(jsonPath("$.statusVerificacaoCpf").value("APROVADO"));
    }

    @Test
    @DisplayName("deveRejeitarNoJobEPermitirCorrigirOCpf")
    void deveRejeitarNoJobEPermitirCorrigirOCpf() throws Exception {
        UsuarioCriado criado = criarUsuarioComToken(UsuarioBuilder.padrao());
        when(verificadorCpfClient.cpfExiste(anyString())).thenReturn(false);

        verificacaoCpfService.processarPendentes();
        assertThat(buscar(criado.id()).getStatusVerificacaoCpf()).isEqualTo(StatusVerificacaoCpf.REJEITADO);

        String novoCpf = UsuarioBuilder.novoCpfValido();
        mockMvc.perform(patch("/api/usuarios/{id}/cpf", criado.id())
                        .header("Authorization", "Bearer " + criado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CpfCorrecaoDTO(novoCpf))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(novoCpf))
                .andExpect(jsonPath("$.statusVerificacaoCpf").value("PENDENTE"));

        when(verificadorCpfClient.cpfExiste(novoCpf)).thenReturn(true);
        verificacaoCpfService.processarPendentes();

        assertThat(buscar(criado.id()).getStatusVerificacaoCpf()).isEqualTo(StatusVerificacaoCpf.APROVADO);
    }

    @Test
    @DisplayName("deveRetornar409AoCorrigirCpfQuandoVerificacaoNaoFoiRejeitada")
    void deveRetornar409AoCorrigirCpfQuandoVerificacaoNaoFoiRejeitada() throws Exception {
        UsuarioCriado criado = criarUsuarioComToken(UsuarioBuilder.padrao());

        mockMvc.perform(patch("/api/usuarios/{id}/cpf", criado.id())
                        .header("Authorization", "Bearer " + criado.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CpfCorrecaoDTO(UsuarioBuilder.novoCpfValido()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("deveManterPendenteEReagendarQuandoProvedorFalha")
    void deveManterPendenteEReagendarQuandoProvedorFalha() throws Exception {
        UsuarioCriado criado = criarUsuarioComToken(UsuarioBuilder.padrao());
        when(verificadorCpfClient.cpfExiste(anyString())).thenThrow(new IllegalStateException("503 simulado"));

        verificacaoCpfService.processarPendentes();

        Usuario usuario = buscar(criado.id());
        assertThat(usuario.getStatusVerificacaoCpf()).isEqualTo(StatusVerificacaoCpf.PENDENTE);
        assertThat(usuario.getTentativasVerificacaoCpf()).isEqualTo(1);
        assertThat(usuario.getProximaVerificacaoCpfEm()).isAfter(Instant.now());

        // Antes do backoff vencer, o job não consulta de novo
        verificacaoCpfService.processarPendentes();
        verify(verificadorCpfClient, times(1)).cpfExiste(anyString());
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
}
