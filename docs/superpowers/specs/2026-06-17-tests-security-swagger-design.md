# Design: Testes Sólidos, Segurança JWT e Swagger — Loja Online

**Data:** 2026-06-17
**Escopo:** Cobertura completa de testes (unit + integração), proteção JWT por rota e documentação OpenAPI

---

## 1. Contexto atual

| Categoria | Coberto | Faltando |
|-----------|---------|----------|
| Unit service | UsuarioService, LoginService | CartaoService, EnderecoService, ProdutoService |
| Unit mapper | — (nenhum) | CartaoMapper, EnderecoMapper, ProdutoMapper |
| Integration controller | UsuarioController, LoginController | CartaoController, EnderecoController, ProdutoController |
| Segurança JWT | `anyRequest().permitAll()` | Configuração granular por rota |
| Swagger | Dependência adicionada mas sem anotações | Tudo |

**Problema raiz:** Valores hardcoded espalhados pelos testes existentes (`"joao@example.com"`, `"12345678901"`, etc.) tornam manutenção frágil quando campos mudam.

---

## 2. Decisões de design

### 2.1 Padrão de Test Data Builders com Datafaker

Adicionar dependência `test` no `pom.xml`:
```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.4.3</version>
    <scope>test</scope>
</dependency>
```

Criar builders de dados de teste centralizados em:
```
src/test/java/br/com/loja_online/builder/
├── CartaoBuilder.java       # CartaoDTO + Cartao model
├── EnderecoBuilder.java     # EnderecoDTO + Endereco model
├── ProdutoBuilder.java      # ProdutoDTO + Produto model
└── UsuarioBuilder.java      # UsuarioCadastroWrapper + LoginDTO
```

**Contrato de cada builder:**
- Instância estática `Faker faker = new Faker(new Locale("pt-BR"))` gera dados realistas
- Valores padrão válidos (passam em todas as anotações `@NotBlank`, `@Min`, etc.)
- Métodos fluentes `com*()` para sobrescrever campos específicos em cada teste
- `buildDto()` retorna o DTO de entrada
- `buildModel()` retorna a entidade (para mocks de repository nos unit tests)

**Exemplo:**
```java
private static final Faker faker = new Faker(new Locale("pt-BR"));

public static CartaoBuilder padrao() {
    return new CartaoBuilder()
        .comNumeroCartao(faker.finance().creditCard()
            .replace("-", "").replace(" ", ""))
        .comNomeCartao(faker.name().fullName())
        .comDataValidade(Date.valueOf(LocalDate.now().plusYears(2)))
        .comCvv(faker.number().numberBetween(100, 999))
        .comDefaultCard(true);
}

// no teste — intenção clara, sem hardcode
CartaoDTO dto = CartaoBuilder.padrao().comCvv(50).buildDto(); // force invalid
CartaoDTO dto = CartaoBuilder.padrao().buildDto();            // valid default
```

**Dados faker por entidade:**
- `CartaoBuilder` → `faker.finance().creditCard()`, `faker.name().fullName()`, `faker.number().numberBetween(100,999)` para CVV
- `EnderecoBuilder` → `faker.address().*` (rua, número, cidade, estado), `faker.numerify("#####-###")` para CEP
- `ProdutoBuilder` → `faker.commerce().productName()`, `faker.commerce().department()`, `faker.commerce().price()`
- `UsuarioBuilder` → `faker.name().fullName()`, `faker.internet().emailAddress()`, `faker.numerify("###########")` para CPF, `faker.internet().password(6, 20)` para senha

### 2.2 SecurityConfig — Matriz de proteção

| Path | Método | Auth |
|------|--------|------|
| `/login/authenticate` | POST | público |
| `/api/usuarios` | POST | público |
| `/produto` | GET | público |
| `/produto/{id}` | GET | público |
| `/v3/api-docs/**`, `/swagger-ui/**` | GET | público |
| `/produto` | POST | JWT obrigatório |
| `/produto/{id}` | DELETE | JWT obrigatório |
| `/api/usuarios/**` | GET, PUT, DELETE | JWT obrigatório |
| `/login/buscar/**` | GET | JWT obrigatório |
| `/cartao/**` | todos | JWT obrigatório |
| `/endereco/**` | todos | JWT obrigatório |

Implementação: substituir `anyRequest().permitAll()` por `requestMatchers` com `.hasAuthority` ou `.authenticated()` para as rotas protegidas. O `JwtAuthenticationFilter` já existe e popula o `SecurityContextHolder`.

### 2.3 AbstractIntegrationTest — helpers JWT

Adicionar ao `AbstractIntegrationTest` dois métodos de apoio:
- `criarUsuarioEObterToken()`: cria um usuário padrão via `UsuarioBuilder`, faz POST `/api/usuarios`, chama POST `/login/authenticate`, devolve o Bearer token
- `obterHeader(String token)`: retorna `HttpHeaders` com `Authorization: Bearer <token>` prontos para `MockMvc`

Esses métodos eliminam o boilerplate em cada teste de integração que precisa de auth.

---

## 3. Estrutura de testes

### 3.1 Unit tests de service

**`CartaoServiceTest`** (Mockito, sem Spring):
- `criarCartaoDeveRetornarCartaoSalvo`
- `getCartaoPorIdDeveRetornarCartaoQuandoExiste`
- `getCartaoPorIdDeveLancarExceptionQuandoNaoExiste`
- `deletarCartaoDeveRemoverQuandoExiste`
- `deletarCartaoDeveLancarExceptionQuandoNaoExiste`
- `atualizarCartaoDeveAtualizarCamposQuandoExiste`
- `atualizarCartaoDeveLancarExceptionQuandoNaoExiste`

**`EnderecoServiceTest`** (Mockito):
- `criarEnderecoDeveRetornarEnderecoSalvo`
- `findByIdDeveRetornarEnderecoQuandoExiste`
- `findByIdDeveLancarExceptionQuandoNaoExiste`
- `deleteByIdDeveRemoverQuandoExiste`
- `deleteByIdDeveLancarExceptionQuandoNaoExiste`

**`ProdutoServiceTest`** (Mockito):
- `findAllDeveRetornarPaginaComProdutos`
- `findAllDeveRetornarPaginaVazia`
- `findByIdDeveRetornarProdutoQuandoExiste`
- `findByIdDeveLancarExceptionQuandoNaoExiste`
- `insertDeveRetornarProdutoSalvo`
- `deleteDeveRemoverQuandoExiste`
- `deleteDeveLancarExceptionQuandoNaoExiste`

### 3.2 Unit tests de mapper

**`CartaoMapperTest`**:
- `paraDtoDeveMapearTodosCamposQuandoCartaoValido`
- `paraDtoDeveRetornarNullQuandoCartaoNull`
- `paraCartaoDeveMapearTodosCamposQuandoDtoValido`

**`EnderecoMapperTest`**:
- `paraDtoDeveMapearTodosCampos`
- `paraEnderecoDeveMapearTodosCampos`

**`ProdutoMapperTest`**:
- `paraDtoDeveMapearTodosCampos`
- `paraProdutoDeveMapearTodosCampos`

### 3.3 Integration tests de controller

Todos estendem `AbstractIntegrationTest` (Testcontainers + PostgreSQL real).

**`CartaoControllerTest`**:
- `POST /cartao/create` com token → 201
- `POST /cartao/create` sem token → 401
- `POST /cartao/create` com dto inválido (cvv < 100) → 400
- `GET /cartao/{id}` com token e id existente → 200
- `GET /cartao/{id}` sem token → 401
- `GET /cartao/{id}` com id inexistente → 404
- `PATCH /cartao/{id}` com token → 200
- `DELETE /cartao/{id}` com token → 204
- `DELETE /cartao/{id}` sem token → 401

**`EnderecoControllerTest`**:
- `POST /endereco/create` com token → 201 com Location header
- `POST /endereco/create` sem token → 401
- `POST /endereco/create` com campos inválidos → 400
- `GET /endereco/{id}` com token e id existente → 200
- `GET /endereco/{id}` sem token → 401
- `GET /endereco/{id}` com id inexistente → 404
- `DELETE /endereco/{id}` com token → 204
- `DELETE /endereco/{id}` sem token → 401

**`ProdutoControllerTest`**:
- `GET /produto` sem token → 200 (rota pública)
- `GET /produto/{id}` sem token → 200
- `GET /produto/{id}` inexistente → 404
- `POST /produto` com token → 201
- `POST /produto` sem token → 401
- `POST /produto` com campos inválidos → 400
- `DELETE /produto/{id}` com token → 204
- `DELETE /produto/{id}` sem token → 401

---

## 4. Swagger / OpenAPI

### 4.1 Config global — `OpenApiConfig.java`

```
src/main/java/br/com/loja_online/config/OpenApiConfig.java
```

Conteúdo:
- `@OpenAPIDefinition` com título "Loja Online API", versão "1.0.0", descrição
- `@SecurityScheme(name = "bearer-jwt", type = HTTP, scheme = "bearer", bearerFormat = "JWT")`

### 4.2 Anotações por controller

Cada controller recebe:
- `@Tag(name = "...", description = "...")` na classe
- `@Operation(summary = "...", description = "...")` em cada método
- `@ApiResponse` para os códigos relevantes (200/201/204/400/401/404/409)
- `@SecurityRequirement(name = "bearer-jwt")` nos endpoints que exigem JWT

### 4.3 Anotações por DTO

Campos com validação recebem `@Schema`:
- `CartaoDTO.cvv` → `minimum = "100"`, `maximum = "9999"`, `example = "123"`
- `CartaoDTO.dataValidade` → `example = "2027-12-31"`
- `CartaoDTO.numeroCartao` → `example = "4111111111111111"`
- `ProdutoDTO.preco` → `example = "99.90"`
- `EnderecoDTO.cep` → `example = "01001-000"`, `pattern = "\\d{5}-\\d{3}"`

---

## 5. Inconsistência detectada: EnderecoController.insert retorna model

`EnderecoController.insert` retorna `ResponseEntity<Endereco>` (entidade JPA), enquanto todos os outros controllers retornam DTO. Isso expõe campos internos e quebra o contrato da API.

**Correção:** Mudar o tipo de retorno para `ResponseEntity<EnderecoDTO>` usando `EnderecoMapper.paraDto()`, igual ao padrão do CartaoController.

---

## 6. Impacto nos testes existentes ao ativar JWT

Quando `SecurityConfig` for alterado, os seguintes testes existentes vão quebrar (atualmente passam porque a rota é `permitAll`):

**`LoginControllerTest`** — rotas que vão exigir token:
- `deveRetornarLoginDTOQuandoGetPorLoginExistente` → GET `/login/buscar/{login}` → 401 sem token
- `deveNaoExporSenhaQuandoGetPorLogin` → idem
- `deveManterIntegridadeQuandoBuscarLoginAposCriacao` → idem
- `devePermitirAcessoSemAutenticacaoQuandoGetPorLogin` → **remover** (comportamento não existe mais)

**`UsuarioControllerTest`** — rotas que vão exigir token:
- `deveRecuperarUsuarioAposCriacaoQuandoPostESeguidoDeGet` → GET `/api/usuarios/{id}` → 401
- `deveRetornar404QuandoGetPorIdInexistente` → GET `/api/usuarios/{id}` → 401
- `deveAtualizarUsuarioQuandoPutValido` → PUT `/api/usuarios/{id}` → 401
- `deveRetornar404QuandoPutParaIdInexistente` → idem
- `deveDeletarUsuarioQuandoDeletePorIdExistente` → DELETE `/api/usuarios/{id}` → 401
- `deveRetornar404QuandoDeletePorIdInexistente` → idem
- `deveRetornar404QuandoDeleteComIdZero` → idem
- `deveAtualizarNomeQuandoPutNomeValido` → idem
- `deveRetornar400QuandoPutComNomeVazio` → idem
- `deveAtualizarTelefoneQuandoPutTelefoneValido` → idem

**Plano de atualização:** Todos esses testes devem chamar `criarUsuarioEObterToken()` no `@BeforeEach` e passar o header `Authorization: Bearer <token>` nas requisições às rotas protegidas.

---

## 7. Ordem de implementação

1. **Builders** — base para tudo, sem dependência de Spring
2. **Fix `EnderecoController.insert`** — mudar para `ResponseEntity<EnderecoDTO>`
3. **SecurityConfig** — antes dos integration tests que testam 401
4. **`AbstractIntegrationTest` helpers** — `criarUsuarioEObterToken()` e `obterHeader(token)`
5. **Atualizar testes existentes** — `LoginControllerTest` e `UsuarioControllerTest` com token
6. **Unit tests de service** — CartaoService, EnderecoService, ProdutoService
7. **Unit tests de mapper** — CartaoMapper, EnderecoMapper, ProdutoMapper
8. **Integration tests novos** — CartaoController, EnderecoController, ProdutoController
9. **OpenApiConfig + anotações Swagger** — controllers + DTOs

---

## 8. Arquivos novos/modificados

**Novos:**
- `src/test/java/br/com/loja_online/builder/CartaoBuilder.java`
- `src/test/java/br/com/loja_online/builder/EnderecoBuilder.java`
- `src/test/java/br/com/loja_online/builder/ProdutoBuilder.java`
- `src/test/java/br/com/loja_online/builder/UsuarioBuilder.java`
- `src/test/java/br/com/loja_online/service/CartaoServiceTest.java`
- `src/test/java/br/com/loja_online/service/EnderecoServiceTest.java`
- `src/test/java/br/com/loja_online/service/ProdutoServiceTest.java`
- `src/test/java/br/com/loja_online/mapper/CartaoMapperTest.java`
- `src/test/java/br/com/loja_online/mapper/EnderecoMapperTest.java`
- `src/test/java/br/com/loja_online/mapper/ProdutoMapperTest.java`
- `src/test/java/br/com/loja_online/controller/CartaoControllerTest.java`
- `src/test/java/br/com/loja_online/controller/EnderecoControllerTest.java`
- `src/test/java/br/com/loja_online/controller/ProdutoControllerTest.java`
- `src/main/java/br/com/loja_online/config/OpenApiConfig.java`

**Modificados:**
- `src/main/java/br/com/loja_online/security/SecurityConfig.java`
- `src/main/java/br/com/loja_online/controller/EnderecoController.java` (fix ResponseEntity<EnderecoDTO> + Swagger)
- `src/test/java/br/com/loja_online/AbstractIntegrationTest.java` (helpers JWT)
- `src/test/java/br/com/loja_online/controller/LoginControllerTest.java` (tokens nas rotas protegidas)
- `src/test/java/br/com/loja_online/controller/UsuarioControllerTest.java` (tokens nas rotas protegidas)
- `src/main/java/br/com/loja_online/controller/CartaoController.java` (anotações Swagger)
- `src/main/java/br/com/loja_online/controller/ProdutoController.java` (anotações Swagger)
- `src/main/java/br/com/loja_online/controller/UsuarioController.java` (anotações Swagger)
- `src/main/java/br/com/loja_online/controller/LoginController.java` (anotações Swagger)
- `src/main/java/br/com/loja_online/dto/CartaoDTO.java` (anotações @Schema)
- `src/main/java/br/com/loja_online/dto/EnderecoDTO.java` (anotações @Schema)
- `src/main/java/br/com/loja_online/dto/ProdutoDTO.java` (anotações @Schema)
