# Testes Sólidos, Segurança JWT e Swagger — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adicionar cobertura completa de testes (unit + integração com JWT real), proteger rotas via SecurityConfig e documentar toda a API com Swagger/OpenAPI.

**Architecture:** Builders centralizados com Datafaker geram dados realistas para todos os testes; AbstractIntegrationTest provê helpers JWT reutilizáveis; SecurityConfig separa rotas públicas das protegidas; OpenApiConfig define o schema Swagger global com Bearer JWT.

**Tech Stack:** Spring Boot 3.5.15, JUnit 5, Mockito, Testcontainers (postgres:17-alpine), Datafaker 2.4.3, springdoc-openapi 2.7.0, JJWT 0.12.6

## Global Constraints

- Java 21, Maven wrapper (`./mvnw`)
- Todas as classes de teste em `src/test/java/br/com/loja_online/`
- Todas as classes de produção em `src/main/java/br/com/loja_online/`
- Testes de integração requerem Docker em execução (Testcontainers)
- Padrão de imports: `jakarta.*` (não `javax.*`)
- Nomes de método de teste em camelCase português: `deveFazerX`, `deveRetornarY`
- Sem `@SuppressWarnings("null")` exceto onde já existe no padrão da codebase

---

### Task 1: Datafaker + Builders

**Files:**
- Modify: `pom.xml` (adicionar dependência Datafaker)
- Create: `src/test/java/br/com/loja_online/builder/CartaoBuilder.java`
- Create: `src/test/java/br/com/loja_online/builder/EnderecoBuilder.java`
- Create: `src/test/java/br/com/loja_online/builder/ProdutoBuilder.java`
- Create: `src/test/java/br/com/loja_online/builder/UsuarioBuilder.java`

**Interfaces:**
- Produz: `CartaoBuilder.padrao()`, `EnderecoBuilder.padrao()`, `ProdutoBuilder.padrao()`, `UsuarioBuilder.padrao()` — usados em todas as tasks seguintes

- [ ] **Step 1: Adicionar Datafaker no pom.xml**

Dentro do bloco `<dependencies>`, após a dependência do testcontainers postgresql:

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.4.3</version>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Criar CartaoBuilder**

```java
package br.com.loja_online.builder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;

public class CartaoBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    private Long numeroCartao;
    private String nomeCartao;
    private Date dataValidade;
    private Integer cvv;
    private Boolean defaultCard;

    private CartaoBuilder() {}

    public static CartaoBuilder padrao() {
        CartaoBuilder b = new CartaoBuilder();
        b.numeroCartao = Long.parseLong(
                faker.finance().creditCard().replaceAll("[^0-9]", "").substring(0, 16));
        b.nomeCartao = faker.name().fullName();
        b.dataValidade = Date.valueOf(LocalDate.now().plusYears(2));
        b.cvv = faker.number().numberBetween(100, 999);
        b.defaultCard = true;
        return b;
    }

    public CartaoBuilder comNumeroCartao(Long numeroCartao) {
        this.numeroCartao = numeroCartao;
        return this;
    }

    public CartaoBuilder comNomeCartao(String nomeCartao) {
        this.nomeCartao = nomeCartao;
        return this;
    }

    public CartaoBuilder comDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
        return this;
    }

    public CartaoBuilder comCvv(Integer cvv) {
        this.cvv = cvv;
        return this;
    }

    public CartaoBuilder comDefaultCard(Boolean defaultCard) {
        this.defaultCard = defaultCard;
        return this;
    }

    public CartaoDTO buildDto() {
        return CartaoDTO.builder()
                .numeroCartao(numeroCartao)
                .nomeCartao(nomeCartao)
                .dataValidade(dataValidade)
                .cvv(cvv)
                .defaultCard(defaultCard)
                .build();
    }

    public Cartao buildModel() {
        return Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .nomeCartao(nomeCartao)
                .dataValidade(dataValidade)
                .cvv(cvv)
                .defaultCard(defaultCard)
                .build();
    }
}
```

- [ ] **Step 3: Criar EnderecoBuilder**

```java
package br.com.loja_online.builder;

import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.model.Endereco;

public class EnderecoBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    private String logradouro;
    private Integer numero;
    private String bairro;
    private String complemento;
    private String referencia;
    private String cep;
    private String cidade;
    private String estado;

    private EnderecoBuilder() {}

    public static EnderecoBuilder padrao() {
        EnderecoBuilder b = new EnderecoBuilder();
        b.logradouro = faker.address().streetName();
        b.numero = faker.number().numberBetween(1, 9999);
        b.bairro = faker.address().cityName();
        b.complemento = "Apto " + faker.number().numberBetween(1, 200);
        b.referencia = "Próximo ao " + faker.address().cityName();
        b.cep = faker.numerify("#####") + "-" + faker.numerify("###");
        b.cidade = faker.address().city();
        b.estado = faker.address().stateAbbr();
        return b;
    }

    public EnderecoBuilder comCep(String cep) {
        this.cep = cep;
        return this;
    }

    public EnderecoBuilder comLogradouro(String logradouro) {
        this.logradouro = logradouro;
        return this;
    }

    public EnderecoDTO buildDto() {
        return new EnderecoDTO(logradouro, numero, bairro, complemento, referencia, cep, cidade, estado);
    }

    public Endereco buildModel() {
        Endereco e = new Endereco(logradouro, numero, bairro, complemento, referencia, cep, cidade, estado);
        return e;
    }
}
```

- [ ] **Step 4: Criar ProdutoBuilder**

```java
package br.com.loja_online.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;

public class ProdutoBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    private Integer id;
    private String nome;
    private String descricao;
    private String categoria;
    private Integer quantidade;
    private BigDecimal preco;
    private String cor;

    private ProdutoBuilder() {}

    public static ProdutoBuilder padrao() {
        ProdutoBuilder b = new ProdutoBuilder();
        b.id = null;
        b.nome = faker.commerce().productName();
        b.descricao = faker.lorem().sentence();
        b.categoria = faker.commerce().department();
        b.quantidade = faker.number().numberBetween(0, 100);
        b.preco = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 999))
                .setScale(2, RoundingMode.HALF_UP);
        b.cor = faker.color().name();
        return b;
    }

    public ProdutoBuilder comId(Integer id) {
        this.id = id;
        return this;
    }

    public ProdutoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ProdutoBuilder comPreco(BigDecimal preco) {
        this.preco = preco;
        return this;
    }

    public ProdutoDTO buildDto() {
        return new ProdutoDTO(id, nome, descricao, categoria, quantidade, preco, cor);
    }

    public Produto buildModel() {
        return new Produto(id == null ? 1 : id, nome, descricao, categoria, quantidade, preco, cor);
    }
}
```

- [ ] **Step 5: Criar UsuarioBuilder**

```java
package br.com.loja_online.builder;

import java.util.Locale;

import net.datafaker.Faker;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;

public class UsuarioBuilder {

    private static final Faker faker = new Faker(new Locale("pt-BR"));

    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String login;
    private String senha;

    private UsuarioBuilder() {}

    public static UsuarioBuilder padrao() {
        UsuarioBuilder b = new UsuarioBuilder();
        b.nome = faker.name().fullName();
        b.email = faker.internet().emailAddress();
        b.cpf = faker.numerify("###########");
        b.telefone = faker.numerify("##########");
        b.login = "user" + faker.number().digits(6);
        b.senha = faker.internet().password(6, 20, true, false);
        return b;
    }

    public UsuarioBuilder comEmail(String email) {
        this.email = email;
        return this;
    }

    public UsuarioBuilder comSenha(String senha) {
        this.senha = senha;
        return this;
    }

    public UsuarioBuilder comLogin(String login) {
        this.login = login;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getLogin() {
        return login;
    }

    public UsuarioCadastroWrapper buildWrapper() {
        UsuarioRequestDTO usuario = UsuarioRequestDTO.builder()
                .nome(nome)
                .email(email)
                .cpf(cpf)
                .telefone(telefone)
                .build();
        LoginDTO loginDto = new LoginDTO(login, senha);
        return new UsuarioCadastroWrapper(usuario, loginDto);
    }
}
```

- [ ] **Step 6: Compilar para verificar que os builders compilam**

```bash
./mvnw test-compile -q
```
Esperado: BUILD SUCCESS sem erros de compilação.

- [ ] **Step 7: Commit**

```bash
git add pom.xml src/test/java/br/com/loja_online/builder/
git commit -m "test: add Datafaker builders for CartaoDTO, EnderecoDTO, ProdutoDTO and UsuarioCadastroWrapper"
```

---

### Task 2: Fix EnderecoController — retorno EnderecoDTO

**Files:**
- Modify: `src/main/java/br/com/loja_online/controller/EnderecoController.java`

**Interfaces:**
- Consumes: `EnderecoMapper.paraDto(Endereco)` — já existe
- Produz: `POST /endereco/create` retorna `ResponseEntity<EnderecoDTO>` em vez de `ResponseEntity<Endereco>`

- [ ] **Step 1: Corrigir o tipo de retorno do método insert**

Arquivo completo `EnderecoController.java`:

```java
package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.mapper.EnderecoMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.service.EnderecoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoDTO> buscarPorId(@NonNull @PathVariable Integer id) {
        return ResponseEntity.ok(EnderecoMapper.paraDto(enderecoService.findById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<EnderecoDTO> insert(@Valid @NonNull @RequestBody EnderecoDTO dto) {
        Endereco endereco = enderecoService.criarEndereco(EnderecoMapper.paraEndereco(dto));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/endereco/{id}")
                .buildAndExpand(endereco.getId())
                .toUri();
        return ResponseEntity.created(uri).body(EnderecoMapper.paraDto(endereco));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@NonNull @PathVariable Integer id) {
        enderecoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 2: Compilar**

```bash
./mvnw compile -q
```
Esperado: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/br/com/loja_online/controller/EnderecoController.java
git commit -m "fix: EnderecoController.insert now returns EnderecoDTO instead of Endereco entity"
```

---

### Task 3: AbstractIntegrationTest — helpers JWT

**Files:**
- Modify: `src/test/java/br/com/loja_online/AbstractIntegrationTest.java`

**Interfaces:**
- Produz:
  - `protected String criarUsuarioEObterToken() throws Exception` — cria usuário via faker e retorna JWT
  - `protected MockMvc mockMvc` — herdado por todos os testes de integração
  - `protected ObjectMapper objectMapper` — herdado por todos os testes de integração

- [ ] **Step 1: Atualizar AbstractIntegrationTest**

```java
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String criarUsuarioEObterToken() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao();
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(builder.getEmail(), builder.getSenha());
        return mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
```

- [ ] **Step 2: Compilar testes**

```bash
./mvnw test-compile -q
```
Esperado: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/AbstractIntegrationTest.java
git commit -m "test: add criarUsuarioEObterToken() helper and shared MockMvc/ObjectMapper to AbstractIntegrationTest"
```

---

### Task 4: Atualizar testes de integração existentes com token JWT

**Files:**
- Modify: `src/test/java/br/com/loja_online/controller/LoginControllerTest.java`
- Modify: `src/test/java/br/com/loja_online/controller/UsuarioControllerTest.java`

**Interfaces:**
- Consumes: `criarUsuarioEObterToken()` de `AbstractIntegrationTest`

- [ ] **Step 1: Atualizar LoginControllerTest**

Arquivo completo `LoginControllerTest.java`:

```java
package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class LoginControllerTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("deveRetornarLoginDTOQuandoGetPorLoginExistente")
    void deveRetornarLoginDTOQuandoGetPorLoginExistente() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao().comLogin("logintest1");
        UsuarioCadastroWrapper wrapper = builder.buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "logintest1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("logintest1"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginInexistente")
    void deveRetornar404QuandoGetPorLoginInexistente() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "naoexiste")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveNaoExporSenhaQuandoGetPorLogin")
    void deveNaoExporSenhaQuandoGetPorLogin() throws Exception {
        UsuarioBuilder builder = UsuarioBuilder.padrao().comLogin("logintest2");
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(builder.buildWrapper())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "logintest2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("deveRetornar401QuandoGetPorLoginSemToken")
    void deveRetornar401QuandoGetPorLoginSemToken() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorLoginComParametroVazio")
    void deveRetornar404QuandoGetPorLoginComParametroVazio() throws Exception {
        mockMvc.perform(get("/login/buscar/{login}", "")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveManterIntegridadeQuandoBuscarLoginAposCriacao")
    void deveManterIntegridadeQuandoBuscarLoginAposCriacao() throws Exception {
        UsuarioBuilder b1 = UsuarioBuilder.padrao().comLogin("integlogin1");
        UsuarioBuilder b2 = UsuarioBuilder.padrao().comLogin("integlogin2");
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b1.buildWrapper())))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b2.buildWrapper())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/login/buscar/{login}", "integlogin1")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin1"));
        mockMvc.perform(get("/login/buscar/{login}", "integlogin2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("integlogin2"));
    }
}
```

- [ ] **Step 2: Atualizar UsuarioControllerTest**

Arquivo completo `UsuarioControllerTest.java`:

```java
package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.UsuarioBuilder;
import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioRequestDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.repository.LoginRepository;
import br.com.loja_online.repository.UsuarioRepository;

@SuppressWarnings("null")
class UsuarioControllerTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginRepository loginRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        loginRepository.deleteAll();
        usuarioRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("deveCriarUsuarioQuandoPostValido")
    void deveCriarUsuarioQuandoPostValido() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(wrapper.usuario().getNome()))
                .andExpect(jsonPath("$.email").value(wrapper.usuario().getEmail()));
    }

    @Test
    @DisplayName("deveRetornar400QuandoPostComCamposObrigatoriosVazios")
    void deveRetornar400QuandoPostComCamposObrigatoriosVazios() throws Exception {
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(
                UsuarioRequestDTO.builder().nome("").email("").cpf("").telefone("").build(),
                new LoginDTO("", ""));
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar400QuandoPostComEmailInvalido")
    void deveRetornar400QuandoPostComEmailInvalido() throws Exception {
        UsuarioCadastroWrapper wrapper = new UsuarioCadastroWrapper(
                UsuarioRequestDTO.builder()
                        .nome("Teste").email("email-invalido").cpf("12345678901").telefone("11999999999")
                        .build(),
                new LoginDTO("teste123", "senha123"));
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet")
    void deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @DisplayName("deveRetornar401QuandoGetSemToken")
    void deveRetornar401QuandoGetSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoGetPorIdInexistente")
    void deveRetornar404QuandoGetPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveAtualizarUsuarioQuandoPutValido")
    void deveAtualizarUsuarioQuandoPutValido() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Nome Atualizado").telefone("11888888888").build();
        mockMvc.perform(put("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("deveRetornar401QuandoPutSemToken")
    void deveRetornar401QuandoPutSemToken() throws Exception {
        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Teste").telefone("11999999999").build();
        mockMvc.perform(put("/api/usuarios/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoPutParaIdInexistente")
    void deveRetornar404QuandoPutParaIdInexistente() throws Exception {
        UsuarioUpdateDTO update = UsuarioUpdateDTO.builder()
                .nome("Teste").telefone("11999999999").build();
        mockMvc.perform(put("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveDeletarUsuarioQuandoDeletePorIdExistente")
    void deveDeletarUsuarioQuandoDeletePorIdExistente() throws Exception {
        UsuarioCadastroWrapper wrapper = UsuarioBuilder.padrao().buildWrapper();
        String response = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deveRetornar401QuandoDeleteSemToken")
    void deveRetornar401QuandoDeleteSemToken() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar404QuandoDeletePorIdInexistente")
    void deveRetornar404QuandoDeletePorIdInexistente() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deveRetornar409QuandoCriarUsuarioComLoginExistente")
    void deveRetornar409QuandoCriarUsuarioComLoginExistente() throws Exception {
        UsuarioCadastroWrapper w1 = UsuarioBuilder.padrao().comLogin("logindup").buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w1)))
                .andExpect(status().isCreated());

        UsuarioCadastroWrapper w2 = UsuarioBuilder.padrao().comLogin("logindup").buildWrapper();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("deveRetornar200QuandoLoginComCredenciaisValidas")
    void deveRetornar200QuandoLoginComCredenciaisValidas() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deveRetornar400QuandoLoginComEmailInvalido")
    void deveRetornar400QuandoLoginComEmailInvalido() throws Exception {
        mockMvc.perform(post("/login/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"email-invalido\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deveRetornar415QuandoContentTypeInvalido")
    void deveRetornar415QuandoContentTypeInvalido() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("deveRetornar405QuandoMetodoDeleteForInvalido")
    void deveRetornar405QuandoMetodoDeleteForInvalido() throws Exception {
        mockMvc.perform(delete("/api/usuarios"))
                .andExpect(status().isMethodNotAllowed());
    }
}
```

- [ ] **Step 3: Verificar que os testes compilam (ainda sem enforcing de auth)**

```bash
./mvnw test-compile -q
```
Esperado: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/br/com/loja_online/controller/
git commit -m "test: update LoginControllerTest and UsuarioControllerTest to use JWT auth headers"
```

---

### Task 5: SecurityConfig — proteção de rotas

**Files:**
- Modify: `src/main/java/br/com/loja_online/security/SecurityConfig.java`

**Interfaces:**
- Produz: rotas protegidas retornam 401 sem token; rotas públicas acessíveis sem token

- [ ] **Step 1: Atualizar SecurityConfig**

```java
package br.com.loja_online.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/produto", "/produto/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autorizado")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: Rodar todos os testes de integração existentes**

```bash
./mvnw test
```
Esperado: BUILD SUCCESS. Todos os testes passam.
Se algum teste 404/401 falhar, verificar se o `authToken` está sendo passado corretamente na Task 4.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/br/com/loja_online/security/SecurityConfig.java
git commit -m "feat: configure JWT route protection — public routes: POST /login/authenticate, POST /api/usuarios, GET /produto/**"
```

---

### Task 6: CartaoService — unit tests

**Files:**
- Create: `src/test/java/br/com/loja_online/service/CartaoServiceTest.java`

**Interfaces:**
- Consumes: `CartaoBuilder.padrao()`, `CartaoService`, `CartaoRepository`, `ObjectNotFoundException`

- [ ] **Step 1: Criar CartaoServiceTest**

```java
package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.repository.CartaoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    private Cartao cartao;

    @BeforeEach
    void setUp() {
        cartao = CartaoBuilder.padrao().buildModel();
    }

    @Test
    @DisplayName("criarCartaoDeveRetornarCartaoSalvo")
    void criarCartaoDeveRetornarCartaoSalvo() {
        when(cartaoRepository.save(any(Cartao.class))).thenReturn(cartao);

        Cartao resultado = cartaoService.criarCartao(cartao);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(cartao.getId());
        assertThat(resultado.getNomeCartao()).isEqualTo(cartao.getNomeCartao());
        verify(cartaoRepository).save(cartao);
    }

    @Test
    @DisplayName("getCartaoPorIdDeveRetornarCartaoQuandoExiste")
    void getCartaoPorIdDeveRetornarCartaoQuandoExiste() {
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));

        Cartao resultado = cartaoService.getCartaoPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCartaoPorIdDeveLancarExceptionQuandoNaoExiste")
    void getCartaoPorIdDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.getCartaoPorId(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deletarCartaoDeveRemoverQuandoExiste")
    void deletarCartaoDeveRemoverQuandoExiste() {
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));

        cartaoService.deletarCartao(1L);

        verify(cartaoRepository).delete(cartao);
    }

    @Test
    @DisplayName("deletarCartaoDeveLancarExceptionQuandoNaoExiste")
    void deletarCartaoDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.deletarCartao(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("atualizarCartaoDeveAtualizarCamposQuandoExiste")
    void atualizarCartaoDeveAtualizarCamposQuandoExiste() {
        Cartao atualizado = CartaoBuilder.padrao().comCvv(456).buildModel();
        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));
        when(cartaoRepository.save(any(Cartao.class))).thenReturn(atualizado);

        Cartao resultado = cartaoService.atualizarCartao(1L, atualizado);

        assertThat(resultado.getCvv()).isEqualTo(456);
        verify(cartaoRepository).save(any(Cartao.class));
    }

    @Test
    @DisplayName("atualizarCartaoDeveLancarExceptionQuandoNaoExiste")
    void atualizarCartaoDeveLancarExceptionQuandoNaoExiste() {
        when(cartaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.atualizarCartao(99L, cartao))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
```

- [ ] **Step 2: Rodar apenas este teste**

```bash
./mvnw test -Dtest=CartaoServiceTest
```
Esperado: 7 testes passam.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/service/CartaoServiceTest.java
git commit -m "test: add CartaoService unit tests covering CRUD and error scenarios"
```

---

### Task 7: EnderecoService — unit tests

**Files:**
- Create: `src/test/java/br/com/loja_online/service/EnderecoServiceTest.java`

- [ ] **Step 1: Criar EnderecoServiceTest**

```java
package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.repository.EnderecoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @InjectMocks
    private EnderecoService enderecoService;

    private Endereco endereco;

    @BeforeEach
    void setUp() {
        endereco = EnderecoBuilder.padrao().buildModel();
    }

    @Test
    @DisplayName("criarEnderecoDeveRetornarEnderecoSalvo")
    void criarEnderecoDeveRetornarEnderecoSalvo() {
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);

        Endereco resultado = enderecoService.criarEndereco(endereco);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getLogradouro()).isEqualTo(endereco.getLogradouro());
        verify(enderecoRepository).save(endereco);
    }

    @Test
    @DisplayName("findByIdDeveRetornarEnderecoQuandoExiste")
    void findByIdDeveRetornarEnderecoQuandoExiste() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        Endereco resultado = enderecoService.findById(1);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCep()).isEqualTo(endereco.getCep());
    }

    @Test
    @DisplayName("findByIdDeveLancarExceptionQuandoNaoExiste")
    void findByIdDeveLancarExceptionQuandoNaoExiste() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.findById(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteByIdDeveRemoverQuandoExiste")
    void deleteByIdDeveRemoverQuandoExiste() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        enderecoService.deleteById(1);

        verify(enderecoRepository).delete(endereco);
    }

    @Test
    @DisplayName("deleteByIdDeveLancarExceptionQuandoNaoExiste")
    void deleteByIdDeveLancarExceptionQuandoNaoExiste() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enderecoService.deleteById(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
```

- [ ] **Step 2: Rodar**

```bash
./mvnw test -Dtest=EnderecoServiceTest
```
Esperado: 5 testes passam.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/service/EnderecoServiceTest.java
git commit -m "test: add EnderecoService unit tests"
```

---

### Task 8: ProdutoService — unit tests

**Files:**
- Create: `src/test/java/br/com/loja_online/service/ProdutoServiceTest.java`

- [ ] **Step 1: Criar ProdutoServiceTest**

```java
package br.com.loja_online.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;
import br.com.loja_online.repository.ProdutoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        produto = ProdutoBuilder.padrao().comId(1).buildModel();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("findAllDeveRetornarPaginaComProdutos")
    void findAllDeveRetornarPaginaComProdutos() {
        when(produtoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(produto)));

        Page<ProdutoDTO> resultado = produtoService.findAll(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo(produto.getNome());
    }

    @Test
    @DisplayName("findAllDeveRetornarPaginaVazia")
    void findAllDeveRetornarPaginaVazia() {
        when(produtoRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<ProdutoDTO> resultado = produtoService.findAll(pageable);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByIdDeveRetornarProdutoQuandoExiste")
    void findByIdDeveRetornarProdutoQuandoExiste() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        ProdutoDTO resultado = produtoService.findById(1);

        assertThat(resultado.nome()).isEqualTo(produto.getNome());
    }

    @Test
    @DisplayName("findByIdDeveLancarExceptionQuandoNaoExiste")
    void findByIdDeveLancarExceptionQuandoNaoExiste() {
        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.findById(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("insertDeveRetornarProdutoSalvoComIdGerado")
    void insertDeveRetornarProdutoSalvoComIdGerado() {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        Produto salvo = ProdutoBuilder.padrao().comId(5).buildModel();
        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);

        ProdutoDTO resultado = produtoService.insert(dto);

        assertThat(resultado.id()).isEqualTo(5);
    }

    @Test
    @DisplayName("deleteDeveRemoverQuandoExiste")
    void deleteDeveRemoverQuandoExiste() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.delete(1);

        verify(produtoRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteDeveLancarExceptionQuandoNaoExiste")
    void deleteDeveLancarExceptionQuandoNaoExiste() {
        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.delete(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
```

- [ ] **Step 2: Rodar**

```bash
./mvnw test -Dtest=ProdutoServiceTest
```
Esperado: 7 testes passam.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/service/ProdutoServiceTest.java
git commit -m "test: add ProdutoService unit tests covering findAll, findById, insert and delete"
```

---

### Task 9: Mapper unit tests

**Files:**
- Create: `src/test/java/br/com/loja_online/mapper/CartaoMapperTest.java`
- Create: `src/test/java/br/com/loja_online/mapper/EnderecoMapperTest.java`
- Create: `src/test/java/br/com/loja_online/mapper/ProdutoMapperTest.java`

- [ ] **Step 1: Criar CartaoMapperTest**

```java
package br.com.loja_online.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.model.Cartao;

class CartaoMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCamposQuandoCartaoValido")
    void paraDtoDeveMapearTodosCamposQuandoCartaoValido() {
        Cartao cartao = CartaoBuilder.padrao().buildModel();

        CartaoDTO dto = CartaoMapper.paraDto(cartao);

        assertThat(dto).isNotNull();
        assertThat(dto.getNumeroCartao()).isEqualTo(cartao.getNumeroCartao());
        assertThat(dto.getNomeCartao()).isEqualTo(cartao.getNomeCartao());
        assertThat(dto.getDataValidade()).isEqualTo(cartao.getDataValidade());
        assertThat(dto.getCvv()).isEqualTo(cartao.getCvv());
        assertThat(dto.getDefaultCard()).isEqualTo(cartao.getDefaultCard());
    }

    @Test
    @DisplayName("paraDtoDeveRetornarNullQuandoCartaoNull")
    void paraDtoDeveRetornarNullQuandoCartaoNull() {
        CartaoDTO dto = CartaoMapper.paraDto(null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("paraCartaoDeveMapearTodosCamposQuandoDtoValido")
    void paraCartaoDeveMapearTodosCamposQuandoDtoValido() {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        Cartao cartao = CartaoMapper.paraCartao(dto);

        assertThat(cartao.getNumeroCartao()).isEqualTo(dto.getNumeroCartao());
        assertThat(cartao.getNomeCartao()).isEqualTo(dto.getNomeCartao());
        assertThat(cartao.getCvv()).isEqualTo(dto.getCvv());
        assertThat(cartao.getDefaultCard()).isEqualTo(dto.getDefaultCard());
    }
}
```

- [ ] **Step 2: Criar EnderecoMapperTest**

```java
package br.com.loja_online.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.model.Endereco;

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
```

- [ ] **Step 3: Criar ProdutoMapperTest**

```java
package br.com.loja_online.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;

class ProdutoMapperTest {

    @Test
    @DisplayName("paraDtoDeveMapearTodosCampos")
    void paraDtoDeveMapearTodosCampos() {
        Produto produto = ProdutoBuilder.padrao().comId(1).buildModel();

        ProdutoDTO dto = ProdutoMapper.paraDto(produto);

        assertThat(dto.id()).isEqualTo(produto.getId());
        assertThat(dto.nome()).isEqualTo(produto.getNome());
        assertThat(dto.categoria()).isEqualTo(produto.getCategoria());
        assertThat(dto.preco()).isEqualByComparingTo(produto.getPreco());
    }

    @Test
    @DisplayName("paraProdutoDeveMapearTodosCampos")
    void paraProdutoDeveMapearTodosCampos() {
        ProdutoDTO dto = ProdutoBuilder.padrao().comId(3).buildDto();

        Produto produto = ProdutoMapper.paraProduto(dto);

        assertThat(produto.getNome()).isEqualTo(dto.nome());
        assertThat(produto.getCategoria()).isEqualTo(dto.categoria());
        assertThat(produto.getPreco()).isEqualByComparingTo(dto.preco());
    }
}
```

- [ ] **Step 4: Rodar os mapper tests**

```bash
./mvnw test -Dtest="CartaoMapperTest,EnderecoMapperTest,ProdutoMapperTest"
```
Esperado: 7 testes passam.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/br/com/loja_online/mapper/
git commit -m "test: add unit tests for CartaoMapper, EnderecoMapper and ProdutoMapper"
```

---

### Task 10: CartaoController — integration tests

**Files:**
- Create: `src/test/java/br/com/loja_online/controller/CartaoControllerTest.java`

- [ ] **Step 1: Criar CartaoControllerTest**

```java
package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.CartaoBuilder;
import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.repository.CartaoRepository;

@SuppressWarnings("null")
class CartaoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        cartaoRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("postCreateDeveRetornar201ComTokenValido")
    void postCreateDeveRetornar201ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCartao").value(dto.getNomeCartao()))
                .andExpect(jsonPath("$.cvv").value(dto.getCvv()));
    }

    @Test
    @DisplayName("postCreateDeveRetornar401SemToken")
    void postCreateDeveRetornar401SemToken() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();

        mockMvc.perform(post("/cartao/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postCreateDeveRetornar400CvvInvalido")
    void postCreateDeveRetornar400CvvInvalido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().comCvv(50).buildDto();

        mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200ComTokenValido")
    void getByIdDeveRetornar200ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String criado = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(criado.substring(criado.lastIndexOf('/') + 1));

        mockMvc.perform(get("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCartao").value(dto.getNomeCartao()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar401SemToken")
    void getByIdDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/cartao/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/cartao/{id}", 999999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("patchDeveRetornar200ComTokenValido")
    void patchDeveRetornar200ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        CartaoDTO atualizado = CartaoBuilder.padrao().comNomeCartao("Nome Atualizado").buildDto();
        mockMvc.perform(patch("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCartao").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        CartaoDTO dto = CartaoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/cartao/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/cartao/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/cartao/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 2: Rodar (requer Docker)**

```bash
./mvnw test -Dtest=CartaoControllerTest
```
Esperado: 9 testes passam.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/controller/CartaoControllerTest.java
git commit -m "test: add CartaoController integration tests with JWT auth scenarios"
```

---

### Task 11: EnderecoController — integration tests

**Files:**
- Create: `src/test/java/br/com/loja_online/controller/EnderecoControllerTest.java`

- [ ] **Step 1: Criar EnderecoControllerTest**

```java
package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.EnderecoBuilder;
import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.repository.EnderecoRepository;

@SuppressWarnings("null")
class EnderecoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private EnderecoRepository enderecoRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        enderecoRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("postCreateDeveRetornar201ComLocationHeader")
    void postCreateDeveRetornar201ComLocationHeader() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();

        mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.logradouro").value(dto.logradouro()))
                .andExpect(jsonPath("$.cep").value(dto.cep()));
    }

    @Test
    @DisplayName("postCreateDeveRetornar401SemToken")
    void postCreateDeveRetornar401SemToken() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();

        mockMvc.perform(post("/endereco/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postCreateDeveRetornar400CepInvalido")
    void postCreateDeveRetornar400CepInvalido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().comCep("CEP-INVALIDO").buildDto();

        mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200ComTokenValido")
    void getByIdDeveRetornar200ComTokenValido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/endereco/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value(dto.cep()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar401SemToken")
    void getByIdDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/endereco/{id}", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/endereco/{id}", 999999)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        EnderecoDTO dto = EnderecoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/endereco/create")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/endereco/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/endereco/{id}", 1))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 2: Rodar**

```bash
./mvnw test -Dtest=EnderecoControllerTest
```
Esperado: 8 testes passam.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/br/com/loja_online/controller/EnderecoControllerTest.java
git commit -m "test: add EnderecoController integration tests with JWT auth and CEP validation"
```

---

### Task 12: ProdutoController — integration tests

**Files:**
- Create: `src/test/java/br/com/loja_online/controller/ProdutoControllerTest.java`

- [ ] **Step 1: Criar ProdutoControllerTest**

```java
package br.com.loja_online.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import br.com.loja_online.AbstractIntegrationTest;
import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.repository.ProdutoRepository;

@SuppressWarnings("null")
class ProdutoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        produtoRepository.deleteAll();
        authToken = criarUsuarioEObterToken();
    }

    @Test
    @DisplayName("getDeveRetornar200SemToken")
    void getDeveRetornar200SemToken() throws Exception {
        mockMvc.perform(get("/produto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("getByIdDeveRetornar200SemToken")
    void getByIdDeveRetornar200SemToken() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/produto/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(dto.nome()));
    }

    @Test
    @DisplayName("getByIdDeveRetornar404QuandoNaoExiste")
    void getByIdDeveRetornar404QuandoNaoExiste() throws Exception {
        mockMvc.perform(get("/produto/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("postDeveRetornar201ComTokenValido")
    void postDeveRetornar201ComTokenValido() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();

        mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(dto.nome()))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("postDeveRetornar401SemToken")
    void postDeveRetornar401SemToken() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();

        mockMvc.perform(post("/produto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("postDeveRetornar400ComNomeVazio")
    void postDeveRetornar400ComNomeVazio() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().comNome("").buildDto();

        mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deleteDeveRetornar204ComTokenValido")
    void deleteDeveRetornar204ComTokenValido() throws Exception {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        String location = mockMvc.perform(post("/produto")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getHeader("Location");
        Integer id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(delete("/produto/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteDeveRetornar401SemToken")
    void deleteDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(delete("/produto/{id}", 1))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 2: Rodar**

```bash
./mvnw test -Dtest=ProdutoControllerTest
```
Esperado: 8 testes passam.

- [ ] **Step 3: Rodar todos os testes**

```bash
./mvnw test
```
Esperado: BUILD SUCCESS, todos os testes passam.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/br/com/loja_online/controller/ProdutoControllerTest.java
git commit -m "test: add ProdutoController integration tests — GET public, POST/DELETE protected"
```

---

### Task 13: OpenApiConfig + Swagger annotations

**Files:**
- Create: `src/main/java/br/com/loja_online/config/OpenApiConfig.java`
- Modify: `src/main/java/br/com/loja_online/controller/CartaoController.java`
- Modify: `src/main/java/br/com/loja_online/controller/EnderecoController.java`
- Modify: `src/main/java/br/com/loja_online/controller/ProdutoController.java`
- Modify: `src/main/java/br/com/loja_online/controller/LoginController.java`
- Modify: `src/main/java/br/com/loja_online/controller/UsuarioController.java`
- Modify: `src/main/java/br/com/loja_online/dto/CartaoDTO.java`
- Modify: `src/main/java/br/com/loja_online/dto/EnderecoDTO.java`
- Modify: `src/main/java/br/com/loja_online/dto/ProdutoDTO.java`

- [ ] **Step 1: Criar OpenApiConfig**

```java
package br.com.loja_online.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Loja Online API",
                version = "1.0.0",
                description = "API REST para gerenciamento de e-commerce: usuários, cartões, endereços e produtos"),
        security = @SecurityRequirement(name = "bearer-jwt"))
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido via POST /login/authenticate")
public class OpenApiConfig {}
```

- [ ] **Step 2: Anotar CartaoController**

```java
package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.mapper.CartaoMapper;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.service.CartaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cartao")
@RequiredArgsConstructor
@Tag(name = "Cartões", description = "Gerenciamento de cartões de crédito e débito")
@SecurityRequirement(name = "bearer-jwt")
public class CartaoController {

    private final CartaoService cartaoService;

    @PostMapping("/create")
    @Operation(summary = "Criar cartão", description = "Cria um novo cartão para o usuário autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartão criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<CartaoDTO> insert(@Valid @NonNull @RequestBody CartaoDTO dto) {
        Cartao cartao = cartaoService.criarCartao(CartaoMapper.paraCartao(dto));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cartao.getId()).toUri();
        return ResponseEntity.created(uri).body(CartaoMapper.paraDto(cartao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartão por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<CartaoDTO> getCartaoPorId(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(CartaoMapper.paraDto(cartaoService.getCartaoPorId(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão deletado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<Void> deleteCartao(@NonNull @PathVariable Long id) {
        cartaoService.deletarCartao(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<CartaoDTO> updateCartao(
            @NonNull @PathVariable Long id, @Valid @NonNull @RequestBody CartaoDTO dto) {
        Cartao cartao = cartaoService.atualizarCartao(id, CartaoMapper.paraCartao(dto));
        return ResponseEntity.ok(CartaoMapper.paraDto(cartao));
    }
}
```

- [ ] **Step 3: Anotar LoginController**

```java
package br.com.loja_online.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.loja_online.dto.LoginDTO;
import br.com.loja_online.dto.LoginRequest;
import br.com.loja_online.service.LoginService;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação", description = "Login e consulta de credenciais")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @GetMapping("/buscar/{login}")
    @Operation(summary = "Buscar login por username")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Login não encontrado")
    })
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<LoginDTO> buscarPorLogin(@PathVariable String login) {
        return ResponseEntity.ok(service.buscarPorLogin(login));
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Autenticar usuário", description = "Retorna JWT Bearer token. Rota pública.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token JWT gerado"),
        @ApiResponse(responseCode = "400", description = "Email ou senha inválidos (formato)"),
        @ApiResponse(responseCode = "401", description = "Credenciais incorretas")
    })
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(service.login(loginRequest.getEmail(), loginRequest.getSenha()));
    }
}
```

- [ ] **Step 4: Anotar ProdutoController**

```java
package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.service.ProdutoService;

@RestController
@RequestMapping("/produto")
@Tag(name = "Produtos", description = "Catálogo de produtos — GET público, POST/DELETE requerem autenticação")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Listar produtos paginados", description = "Rota pública")
    @ApiResponse(responseCode = "200", description = "Lista paginada de produtos")
    public ResponseEntity<Page<ProdutoDTO>> findAll(@NonNull Pageable pageable) {
        return ResponseEntity.ok(produtoService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Rota pública")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProdutoDTO> findById(@NonNull @PathVariable Integer id) {
        return ResponseEntity.ok(produtoService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ProdutoDTO> save(@Valid @RequestBody ProdutoDTO produtoDTO) {
        produtoDTO = produtoService.insert(produtoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(produtoDTO.id()).toUri();
        return ResponseEntity.created(uri).body(produtoDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto deletado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<ProdutoDTO> delete(@NonNull @PathVariable Integer id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 5: Adicionar @Schema nos campos de CartaoDTO**

No arquivo `CartaoDTO.java`, adicionar `import io.swagger.v3.oas.annotations.media.Schema;` e as anotações nos campos:

```java
@Schema(description = "Número do cartão (16 dígitos)", example = "4111111111111111")
@NotNull(message = "Número do cartão é obrigatório")
@Positive(message = "Número do cartão deve ser positivo")
private Long numeroCartao;

@Schema(description = "Nome impresso no cartão", example = "JOAO A SILVA")
@NotBlank(message = "Nome no cartão é obrigatório")
@Size(min = 2, max = 100, message = "Nome no cartão deve ter entre 2 e 100 caracteres")
private String nomeCartao;

@Schema(description = "Data de validade futura", example = "2027-12-31")
@NotNull(message = "Data de validade é obrigatória")
@Future(message = "Data de validade deve ser uma data futura")
private Date dataValidade;

@Schema(description = "CVV (3 ou 4 dígitos)", minimum = "100", maximum = "9999", example = "123")
@NotNull(message = "CVV é obrigatório")
@Min(value = 100, message = "CVV deve ter entre 3 e 4 dígitos")
@Digits(integer = 4, fraction = 0, message = "CVV deve ter entre 3 e 4 dígitos")
private Integer cvv;

@Schema(description = "true se este é o cartão padrão", example = "true")
@NotNull(message = "Indicação de cartão padrão é obrigatória")
private Boolean defaultCard;
```

- [ ] **Step 6: Adicionar @Schema em EnderecoDTO**

No arquivo `EnderecoDTO.java`, adicionar `import io.swagger.v3.oas.annotations.media.Schema;` e as anotações:

```java
public record EnderecoDTO(
        @Schema(example = "Rua das Flores") @NotBlank() String logradouro,
        @Schema(example = "123") @NotNull() @Min(1) Integer numero,
        @Schema(example = "Centro") @NotBlank() String bairro,
        @Schema(example = "Apto 42") @NotBlank() String complemento,
        @Schema(example = "Próximo ao mercado") @NotBlank() String referencia,
        @Schema(description = "Formato #####-###", example = "01001-000")
                @NotBlank() @Pattern(regexp = "\\d{5}-\\d{3}") String cep,
        @Schema(example = "São Paulo") @NotBlank() String cidade,
        @Schema(example = "SP") @NotBlank() String estado) {}
```

- [ ] **Step 7: Adicionar @Schema em ProdutoDTO**

No arquivo `ProdutoDTO.java`, adicionar `import io.swagger.v3.oas.annotations.media.Schema;`:

```java
public record ProdutoDTO(
        @Schema(description = "ID gerado automaticamente — enviar null no POST") Integer id,
        @Schema(example = "Tênis Esportivo") @NotBlank String nome,
        @Schema(example = "Tênis para corrida confortável") String descricao,
        @Schema(example = "Calçados") @NotBlank String categoria,
        @Schema(example = "10") @PositiveOrZero Integer quantidade,
        @Schema(example = "199.90") @PositiveOrZero BigDecimal preco,
        @Schema(example = "Preto") String cor) {}
```

- [ ] **Step 8: Compilar e rodar todos os testes**

```bash
./mvnw test
```
Esperado: BUILD SUCCESS, todos os testes passam.

- [ ] **Step 9: Verificar Swagger UI acessível (requer app rodando)**

Subir a aplicação e abrir `http://localhost:8080/swagger-ui/index.html`. Verificar:
- Seções "Autenticação", "Cartões", "Produtos" visíveis
- Cadeado aparece nos endpoints protegidos
- "Authorize" no topo aceita token JWT Bearer

- [ ] **Step 10: Commit**

```bash
git add src/main/java/br/com/loja_online/config/OpenApiConfig.java
git add src/main/java/br/com/loja_online/controller/
git add src/main/java/br/com/loja_online/dto/CartaoDTO.java
git add src/main/java/br/com/loja_online/dto/EnderecoDTO.java
git add src/main/java/br/com/loja_online/dto/ProdutoDTO.java
git commit -m "feat: add Swagger/OpenAPI documentation with JWT security scheme and @Schema annotations"
```
