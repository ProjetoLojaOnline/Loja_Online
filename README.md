# Loja Online API

API REST de e-commerce com autenticação JWT. Projeto da comunidade **Debug & Grow** para estudos.

---

## Índice

1. [Tecnologias e Dependências](#1-tecnologias-e-dependências)
2. [Arquitetura - Passo a Passo](#2-arquitetura---passo-a-passo)
3. [Camada de Model (Entidades JPA)](#3-camada-de-model-entidades-jpa)
4. [Camada de DTO](#4-camada-de-dto)
5. [Camada de Mapper](#5-camada-de-mapper)
6. [Camada de Repository](#6-camada-de-repository)
7. [Camada de Service](#7-camada-de-service)
8. [Camada de Controller](#8-camada-de-controller)
9. [Camada de Security (Autenticação JWT)](#9-camada-de-security-autenticação-jwt)
10. [Camada de Exception Handler](#10-camada-de-exception-handler)
11. [Fluxo Completo de Autenticação](#11-fluxo-completo-de-autenticação)
12. [Endpoints da API](#12-endpoints-da-api)
13. [Como Rodar](#13-como-rodar)
14. [Variáveis de Ambiente](#14-variáveis-de-ambiente)
15. [Exemplos de Uso](#15-exemplos-de-uso)
16. [Testes](#16-testes)
17. [Swagger](#17-swagger)

---

## 1. Tecnologias e Dependências

### Stack Principal

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.5.6 | Framework principal |
| Spring Web | - | API REST |
| Spring Data JPA | - | ORM / Hibernate |
| Spring Security | - | Autenticação e autorização |
| Spring Validation | - | Validação de DTOs (`@NotBlank`, `@Email`, etc.) |
| MySQL 8.0 | - | Banco de dados (produção) |
| H2 | - | Banco em memória (testes) |

### Dependências do `pom.xml`

| Grupo | Artefato | Versão | Escopo |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-data-jpa` | 3.5.6 | - |
| `org.springframework.boot` | `spring-boot-starter-security` | 3.5.6 | - |
| `org.springframework.boot` | `spring-boot-starter-validation` | 3.5.6 | - |
| `org.springframework.boot` | `spring-boot-starter-web` | 3.5.6 | - |
| `org.springframework.boot` | `spring-boot-devtools` | 3.5.6 | runtime |
| `com.mysql` | `mysql-connector-j` | - | runtime |
| `org.projectlombok` | `lombok` | - | optional |
| `com.auth0` | `java-jwt` | 4.5.1 | - |
| `org.springdoc` | `springdoc-openapi-starter-webmvc-ui` | 2.7.0 | - |
| `org.springframework.boot` | `spring-boot-starter-test` | 3.5.6 | test |
| `com.h2database` | `h2` | - | test |
| `org.springframework.security` | `spring-security-test` | - | test |

---

## 2. Arquitetura - Passo a Passo

O projeto segue o padrão **Arquitetura em Camadas**. Cada camada tem uma responsabilidade específica e se comunica com a camada abaixo:

```
[Cliente HTTP]
      ↓
[1. Controller]   ←  Recebe requisição, valida entrada (DTO), delega ao Service
      ↓
[2. DTO]          ←  Objeto de transporte entre as camadas
      ↓
[3. Service]      ←  Lógica de negócio, regras, transações
      ↓
[4. Mapper]       ←  Converte Entity ↔ DTO
      ↓
[5. Repository]   ←  Acesso ao banco (JPA)
      ↓
[6. Model]        ←  Entidades JPA (tabelas do banco)
      ↓
[ MySQL ]
```

**Camadas transversais (afetam todas as anteriores):**

```
[Security]        ←  Filtro JWT intercepta requisições, autentica e autoriza
[Exception]       ←  Captura exceções de todas as camadas e retorna JSON padronizado
```

---

## 3. Camada de Model (Entidades JPA)

Pacote: `br.com.loja_online.model`

Cada classe abaixo é uma **tabela no banco MySQL**. O `ddl-auto=update` cria/atualiza as tabelas automaticamente.

### 3.1. `Login` → Tabela `login`

Implementa `UserDetails` (Spring Security). É a **credencial de autenticação**.

| Campo | Tipo | Coluna | Restrição |
|---|---|---|---|
| `id` | `Long` | `id` | PK, auto increment |
| `login` | `String` | `login` | **Unique**, not null |
| `senha` | `String` | `senha` | BCrypt, not null |
| `role` | `Role` (enum) | `role` | `ROLE_USER`, `ROLE_ADMIN`, `ROLE_VENDEDOR` |
| `usuario` | `Usuario` | `id_usuario` (FK) | `@OneToOne` |

**Métodos do `UserDetails`:**

| Método | Retorno |
|---|---|
| `getUsername()` | `login` (campo login) |
| `getPassword()` | `senha` |
| `getAuthorities()` | `ROLE_*` como `GrantedAuthority` |

### 3.2. `Usuario` → Tabela `tb_usuario`

Dados de perfil do usuário. **Não é usado para autenticação.**

| Campo | Tipo | Restrição |
|---|---|---|
| `id` | `Long` | PK, auto increment |
| `nome` | `String` | - |
| `telefone` | `String` | - |
| `email` | `String` | **Unique** (validado no cadastro) |
| `cpf` | `String` | **Unique** (validado no cadastro) |
| `dataNascimento` | `String` | - |
| `genero` | `String` | - |
| `foto` | `String` | URL da foto |
| `tipo` | `String` | - |
| `login` | `Login` | `@OneToOne(mappedBy = "usuario")` |
| `cartoes` | `List<Cartao>` | `@OneToMany` |
| `enderecos` | `List<Endereco>` | `@OneToMany` |

### 3.3. `Produto` → Tabela `tb_produto`

| Campo | Tipo |
|---|---|
| `id` | `Integer` |
| `nome` | `String` |
| `descricao` | `String` |
| `categoria` | `String` |
| `quantidade` | `Integer` |
| `preco` | `BigDecimal` |
| `cor` | `String` |

### 3.4. `Cartao` → Tabela `tb_cartao`

| Campo | Tipo | Relacionamento |
|---|---|---|
| `id` | `Long` | PK |
| `numeroCartao` | `Long` | - |
| `nomeCartao` | `String` | - |
| `dataValidade` | `Date` | - |
| `cvv` | `Integer` | - |
| `defaultCard` | `Boolean` | - |
| `usuario` | `Usuario` | `@ManyToOne` → `tb_usuario` |

### 3.5. `Endereco` → Tabela `endereco`

| Campo | Tipo | Relacionamento |
|---|---|---|
| `id` | `Integer` | PK |
| `logradouro` | `String` | - |
| `numero` | `Integer` | - |
| `bairro` | `String` | - |
| `complemento` | `String` | - |
| `referencia` | `String` | - |
| `cep` | `String` | - |
| `cidade` | `String` | - |
| `estado` | `String` | - |
| `usuario` | `Usuario` | `@ManyToOne` → `tb_usuario` |

### 3.6. `Carrinho` → Tabela `carrinho`

| Campo | Tipo |
|---|---|
| `id` | `Long` |
| `produtos` | `List<Produto>` (`@ManyToMany`) |
| `valorTotal` | `double` |
| `valorFrete` | `double` |

### 3.7. `Pedido` → Tabela `tb_pedido`

| Campo | Tipo |
|---|---|
| `id` | `Integer` |
| `listaProdutos` | `List<ItemVenda>` (`@OneToMany`) |
| `valorTotal` | `BigDecimal` |
| `valorFrete` | `BigDecimal` |
| `status` | `PedidoStatus` (enum) |
| `codigoRastreio` | `String` |

### 3.8. `ItemVenda` → Tabela `tb_itens_venda`

| Campo | Tipo | Relacionamento |
|---|---|---|
| `id` | `Integer` | PK |
| `pedido` | `Pedido` | `@ManyToOne` → `tb_pedido` |
| `produto` | `Produto` | `@ManyToOne` → `tb_produto` |
| `quantidade` | `Integer` | - |
| `precoUnitario` | `BigDecimal` | - |

### 3.9. Enums

#### `Role`
```
ROLE_ADMIN, ROLE_VENDEDOR, ROLE_USER
```

#### `PedidoStatus`
```
CONFIRMADO, CANCELADO, AGUARDANDO_PAGAMENTO, EM_SEPARACAO, EM_TRANSPORTE
```

---

## 4. Camada de DTO

Pacote: `br.com.loja_online.dto`

Os DTOs (Data Transfer Objects) transportam dados entre as camadas, separando a representação interna (entity) da externa (JSON).

### 4.1. `LoginDTO` (record)

Usado para **login** (`POST /auth`) e como parte do **cadastro**.

```java
public record LoginDTO(
    @NotBlank String login,
    @NotBlank @Size(min = 6, max = 72) String senha
) {}
```

### 4.2. `UsuarioRequestDTO`

Usado no **cadastro** (`POST /api/usuarios`), parte `usuario` do JSON.

```java
public class UsuarioRequestDTO {
    @NotBlank String nome;
    @NotBlank @Size(min=10, max=11) String telefone;
    @NotBlank @Email String email;
    @NotBlank @Size(min=11, max=11) String cpf;
    String dataNascimento, genero, foto, tipo;
    List<Cartao> cartoes;
    List<Endereco> enderecos;
}
```

### 4.3. `UsuarioCadastroWrapper` (record)

**Wrapper** que combina `UsuarioRequestDTO` + `LoginDTO` em um único JSON.

```java
public record UsuarioCadastroWrapper(
    @Valid UsuarioRequestDTO usuario,
    @Valid LoginDTO login
) {}
```

### 4.4. `UsuarioResponseDTO`

Resposta da API com dados do usuário (sem senha nem role).

```java
public class UsuarioResponseDTO {
    Long id;
    String nome, telefone, email, cpf;
    String dataNascimento, genero, foto, tipo;
    List<Cartao> cartoes;
    List<Endereco> enderecos;
}
```

### 4.5. `UsuarioUpdateDTO` (record)

```java
public record UsuarioUpdateDTO(
    @NotBlank String nome,
    @NotBlank @Size(min=10, max=11) String telefone,
    String foto,
    String genero
) {}
```

### 4.6. `DadosToken` (record - pacote security)

```java
public record DadosToken(String token) {}
```

Resposta do `POST /auth` contendo o JWT.

---

## 5. Camada de Mapper

Pacote: `br.com.loja_online.mapper`

Converte entidades (Model) em DTOs e vice-versa.

### 5.1. `UsuarioMapper`

| Método | Origem → Destino |
|---|---|
| `paraDTO(Usuario)` | `Usuario` → `UsuarioResponseDTO` |
| `paraUsuario(UsuarioRequestDTO)` | `UsuarioRequestDTO` → `Usuario` (inclui cartões e endereços vinculados) |

### 5.2. `LoginMapper`

| Método | Origem → Destino |
|---|---|
| `paraDTO(Login)` | `Login` → `LoginDTO` (senha = null por segurança) |
| `paraLogin(LoginDTO)` | `LoginDTO` → `Login` (apenas login + senha, sem role) |

### 5.3. `UsuarioUpadateMapper`

| Método | Origem → Destino |
|---|---|
| `updateUsuarioDTO(UsuarioUpdateDTO, Usuario)` | Faz merge dos dados do DTO na entidade existente |

---

## 6. Camada de Repository

Pacote: `br.com.loja_online.repository`

Interfaces que estendem `JpaRepository` — o Spring Data JPA implementa automaticamente.

### 6.1. `LoginRepository`

| Método | Query |
|---|---|
| `findByLogin(String)` | `SELECT * FROM login WHERE login = ?` |
| `existsByLogin(String)` | `SELECT COUNT(*) FROM login WHERE login = ?` |

### 6.2. `UsuarioRepository`

| Método | Query |
|---|---|
| `findByLogin_Login(String)` | Busca Usuario pelo campo `login.login` |
| `findByEmail(String)` | `SELECT * FROM tb_usuario WHERE email = ?` |
| `existsByEmail(String)` | Verifica se email já existe |
| `existsByCpf(String)` | Verifica se CPF já existe |
| `existsByTelefone(String)` | Verifica se telefone já existe |

---

## 7. Camada de Service

Pacote: `br.com.loja_online.service`

### 7.1. `UsuarioService`

**Responsabilidade:** CRUD de usuários + regras de cadastro.

**Métodos principais:**

| Método | Descrição |
|---|---|
| `insert(UsuarioRequestDTO, LoginDTO)` | Cadastra novo usuário + credencial de login |
| `findAll()` | Lista todos usuários |
| `findById(Long)` | Busca por ID |
| `findByLogin(String)` | Busca pelo campo login |
| `update(Long, UsuarioUpdateDTO)` | Atualiza dados do usuário |
| `deleteById(Long)` | Remove usuário |

**Regras do `insert()` (passo a passo):**

```
1. Verifica se login já existe     → 409 Conflict se existir
2. Verifica se email já existe      → 409 Conflict se existir
3. Verifica se CPF já existe        → 409 Conflict se existir
4. Verifica se telefone já existe   → 409 Conflict se existir
5. Mapeia DTOs para entidades
6. Cria Login com senha BCrypt e role ROLE_USER
7. Associa Login ↔ Usuario (bidirecional)
8. Salva Usuario (cascade persiste Login também)
9. Retorna UsuarioResponseDTO
```

### 7.2. `AutenticacaoService`

**Responsabilidade:** Implementar `UserDetailsService` do Spring Security.

**Método único:**

```java
UserDetails loadUserByUsername(String username)
```

- Recebe o `login` (username digitado)
- Busca no banco via `LoginRepository.findByLogin(login)`
- Se não encontrar → lança `AuthenticationException` (estende `UsernameNotFoundException` do Spring Security)
- Se encontrar → retorna o `Login` (que é um `UserDetails`)

### 7.3. `LoginService`

**Responsabilidade:** Consultar dados de login (rota auxiliar).

| Método | Descrição |
|---|---|
| `buscarPorLogin(String)` | Busca login e retorna `LoginDTO` (senha = null) |

---

## 8. Camada de Controller

Pacote: `br.com.loja_online.controller`

### 8.1. `AutenticacaoController` → `POST /auth`

**Endpoint público** que gera o token JWT.

**Passo a passo do login:**

```
1. Recebe { "login": "...", "senha": "..." } (LoginDTO)
2. Cria UsernamePasswordAuthenticationToken
3. Chama authenticationManager.authenticate(token)
4. AuthenticationManager delega para AutenticacaoService.loadUserByUsername()
5. Busca Login no banco pelo campo "login"
6. Compara senha (BCryptPasswordEncoder)
7. Se válido → TokenService.gerarToken(login) → JWT
8. Retorna { "token": "eyJhbGci..." }
```

### 8.2. `UsuarioController` → `/api/usuarios`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/api/usuarios` | Lista todos | ✅ |
| GET | `/api/usuarios/{id}` | Busca por ID | ✅ |
| GET | `/api/usuarios/login/{login}` | Busca por login | ✅ |
| POST | `/api/usuarios` | Cadastro | ❌ (público) |
| PUT | `/api/usuarios/{id}` | Atualiza | ✅ |
| DELETE | `/api/usuarios/{id}` | Deleta | ✅ |

> O cadastro é público — `POST /api/usuarios` está liberado no `SecurityConfig`.

### 8.3. `LoginController` → `/login`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/login/buscar/{login}` | Busca login por nome | ✅ |

---

## 9. Camada de Security (Autenticação JWT)

Pacote: `br.com.loja_online.security`

### 9.1. `CorsConfig`

Libera CORS para qualquer origem (`*`), métodos GET, POST, PUT, DELETE, OPTIONS.

### 9.2. `SecurityConfig`

Configuração Spring Security:

```
- CSRF desabilitado (API stateless)
- Sessão STATELESS (não usa sessão HTTP)
- Rotas públicas: POST `/auth`, POST `/api/usuarios`
- Demais rotas: exigem autenticação
- Form login desabilitado
- Filtro JWT (SecurityFilter) adicionado antes do UsernamePasswordAuthenticationFilter
- Beans: AuthenticationManager, PasswordEncoder (BCrypt)
```

### 9.3. `TokenService`

**Geração do JWT:**

```
1. Pega o secret da propriedade api.security.token.secret (variável de ambiente JWT_SECRET)
2. Valida se secret tem 32+ caracteres (senão, RuntimeException na inicialização)
3. Cria algoritmo HMAC256 com o secret
4. Gera JWT com:
   - Issuer: "loja-online"
   - Subject: login.getLogin() (o valor do campo "login")
   - Expiração: 2 horas
   - Assinado com HMAC256
```

**Verificação do JWT:**

```
1. Extrai token do header "Authorization: Bearer <token>"
2. Verifica assinatura usando o mesmo secret
3. Verifica issuer ("loja-online")
4. Extrai subject (campo "login" do usuário)
```

### 9.4. `SecurityFilter` (Filtro JWT)

Intercepta **todas as requisições** e valida o token:

```
1. Extrai token do header "Authorization: Bearer <token>"
2. Se token existe:
   a. TokenService.getSubject(token) → extrai "login"
   b. LoginRepository.findByLogin(login) → busca usuário no banco
   c. Cria UsernamePasswordAuthenticationToken com o Login + authorities
   d. Seta no SecurityContextHolder (usuário autenticado)
3. Se token não existe → requisição segue sem autenticação
4. Se erro (token inválido/expirado) → limpa contexto (não lança exceção, só desautentica)
```

### 9.5. `DadosToken` (record)

```java
public record DadosToken(String token) {}
```

---

## 10. Camada de Exception Handler

Pacote: `br.com.loja_online.exception`

### 10.1. Exceções customizadas

| Classe | Uso |
|---|---|
| `ObjectNotFoundException` | Registro não encontrado (404) |
| `ConflictException` | Violação de unicidade (409) |
| `AuthenticationException` (extends `UsernameNotFoundException`) | Credenciais inválidas (401) |

### 10.2. `ControllerAdviceHandler`

Tratamento global com `@ControllerAdvice`:

| Exceção | HTTP Status | Body |
|---|---|---|
| `ObjectNotFoundException` | 404 | `StandardError` |
| `MethodArgumentNotValidException` | 400 | `ValidationError` (com lista de erros por campo) |
| `ConflictException` | 409 | `StandardError` |
| `BadCredentialsException` | 401 | `StandardError("Credenciais inválidas")` |
| `AuthenticationException` (Spring) | 401 | `StandardError("Credenciais inválidas")` |

### 10.3. Modelos de erro

```java
StandardError { timestamp, status, error, message, path }
ValidationError extends StandardError { List<FieldMessage> errors }
FieldMessage { fieldName, message }
```

---

## 11. Fluxo Completo de Autenticação

### Cadastro de Usuário

```
Cliente                          Servidor
  │                                │
  │  POST /api/usuarios            │
  │  {                             │
  │    "usuario": { ... },         │
  │    "login": {                  │
  │      "login": "joao@email.com",│  ← campo "login" da credencial
  │      "senha": "123456"         │
  │    }                           │
  │  }                             │
  │ ─────────────────────────────> │
  │                                │
  │                    UsuarioService.insert()
  │                    ├─ Valida unicidade (login, email, CPF, telefone)
  │                    ├─ Cria Usuario (tb_usuario)
  │                    ├─ Cria Login (tabela login)
  │                    │   ├─ login = "joao@email.com"
  │                    │   ├─ senha = BCrypt("123456")
  │                    │   └─ role = ROLE_USER
  │                    ├─ Associa Login ↔ Usuario
  │                    └─ Salva (cascade)
  │                                │
  │  201 Created                   │
  │  { "id": 1, "nome": "João",   │
  │    "email": "joao@email.com",  │
  │    ... }                       │
  │ <───────────────────────────── │
```

### Login (gerar JWT)

```
Cliente                          Servidor
  │                                │
  │  POST /auth                    │
  │  {                             │
  │    "login": "joao@email.com",  │
  │    "senha": "123456"           │
  │  }                             │
  │ ─────────────────────────────> │
  │                                │
  │   AutenticacaoController       │
  │   ├─ Cria UsernamePasswordAuthenticationToken
  │   └─ authenticationManager.authenticate(token)
  │                                │
  │   AutenticacaoService          │
  │   (UserDetailsService)         │
  │   ├─ LoginRepository.findByLogin("joao@email.com")
  │   ├─ BCryptPasswordEncoder.matches("123456", hash)
  │   └─ Retorna Login (UserDetails)
  │                                │
  │   TokenService                 │
  │   ├─ Algorithm.HMAC256(secret) │
  │   ├─ JWT.create()              │
  │   │   .withSubject(login)      │ ← subject = "joao@email.com"
  │   │   .withIssuer("loja-online")│
  │   │   .withExpiresAt(+2h)      │
  │   │   .sign(algoritmo)         │
  │   └─ Retorna token JWT         │
  │                                │
  │  200 OK                        │
  │  { "token": "eyJhbGci..." }   │
  │ <───────────────────────────── │
```

### Requisição Autenticada

```
Cliente                          Servidor
  │                                │
  │  GET /api/usuarios             │
  │  Authorization: Bearer <JWT>   │
  │ ─────────────────────────────> │
  │                                │
  │   SecurityFilter.doFilter()    │
  │   ├─ Extrai token do header    │
  │   ├─ TokenService.getSubject() │ → "joao@email.com"
  │   ├─ LoginRepository.findByLogin() → Login
  │   ├─ Cria UsernamePasswordAuthenticationToken
  │   └─ Seta SecurityContext      │
  │                                │
  │   UsuarioController.listar()   │
  │   └─ UsuarioService.findAll()  │
  │                                │
  │  200 OK                        │
  │  [ { "id": 1, ... }, ... ]    │
  │ <───────────────────────────── │
```

---

## 12. Endpoints da API

### Autenticação

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/auth` | Login → retorna JWT | ❌ |

### Usuários

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/api/usuarios` | Lista todos | ✅ |
| GET | `/api/usuarios/{id}` | Busca por ID | ✅ |
| GET | `/api/usuarios/login/{login}` | Busca por login | ✅ |
| POST | `/api/usuarios` | Cadastro | ❌ |
| PUT | `/api/usuarios/{id}` | Atualiza | ✅ |
| DELETE | `/api/usuarios/{id}` | Deleta | ✅ |

### Produtos

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/produto` | Lista (paginado) | ✅ |
| GET | `/produto/{id}` | Busca por ID | ✅ |
| POST | `/produto` | Cria | ✅ |
| DELETE | `/produto/{id}` | Deleta | ✅ |

### Cartões

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/cartao/create` | Cadastra | ✅ |
| GET | `/cartao/{id}` | Busca por ID | ✅ |
| PATCH | `/cartao/{id}` | Atualiza | ✅ |
| DELETE | `/cartao/{id}` | Deleta | ✅ |

### Endereços

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/endereco/create` | Cadastra | ✅ |
| GET | `/endereco/{id}` | Busca por ID | ✅ |

### Login (consulta)

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/login/buscar/{login}` | Busca login | ✅ |

---

## 13. Como Rodar

### Opção 1: Docker Compose (recomendado)

```bash
docker compose up -d
# API em http://localhost:8080
```

```bash
docker compose down   # Parar
```

### Opção 2: Manual (MySQL local)

```bash
# 1. Crie o banco MySQL
mysql -u root -p -e "CREATE DATABASE loja_online;"

# 2. Configure variáveis de ambiente
export DB_URL=jdbc:mysql://localhost:3306/loja_online
export DB_USER=root
export DB_PASSWORD=sua_senha
export JWT_SECRET=umaChaveComPeloMenos32CaracteresAqui

# 3. Execute
./mvnw spring-boot:run
```

---

## 14. Variáveis de Ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DB_URL` | Não | `jdbc:mysql://db:3306/loja_online` | URL do banco |
| `DB_USER` | Não | `root` | Usuário MySQL |
| `DB_PASSWORD` | Não | `root` | Senha MySQL |
| `JWT_SECRET` | **Sim** | - | Chave secreta JWT (mínimo 32 caracteres) |

---

## 15. Exemplos de Uso

### Cadastrar Usuário

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": {
      "nome": "João Silva",
      "telefone": "11999999999",
      "email": "joao@email.com",
      "cpf": "12345678901",
      "dataNascimento": "1990-01-01",
      "genero": "Masculino",
      "tipo": "Cliente"
    },
    "login": {
      "login": "joao@email.com",
      "senha": "123456"
    }
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/auth \
  -H "Content-Type: application/json" \
  -d '{
    "login": "joao@email.com",
    "senha": "123456"
  }'
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Listar Usuários (autenticado)

```bash
curl http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Buscar Usuário por Login

```bash
curl http://localhost:8080/api/usuarios/login/joao@email.com \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 16. Testes

```bash
./mvnw test
```

Usa **H2 em memória** (perfil `test`). Cobre:
- Autenticação (credenciais válidas, senha errada, usuário inexistente)
- Acesso a rotas protegidas sem token
- Services (CRUD de usuários, login)

---

## 17. Swagger

Com a aplicação rodando:

```
http://localhost:8080/swagger-ui.html
```

- Documentação interativa de todos os endpoints
- Teste requisições diretamente pelo navegador
- Use **Authorize** (botão no topo) com `Bearer <token>` para testar rotas protegidas

---

## Projeto Desenvolvido pela Comunidade Debug & Grow

- Para estudos e aprendizado
- Para compartilhar conhecimento
- Para ajudar outros desenvolvedores
- Para melhorar nossas habilidades
- Para cada um ajudar outro a crescer
- Para Networking
