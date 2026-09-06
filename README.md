# Loja Online — debug & grow

> Backend REST de e-commerce open-source, criado pela comunidade **debug & grow** para quem quer aprender desenvolvimento profissional na prática — do código ao pull request.

[![CI](https://github.com/ProjetoLojaOnline/Loja_Online/actions/workflows/ci.yml/badge.svg)](https://github.com/ProjetoLojaOnline/Loja_Online/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Sumário

- [O que é este projeto?](#o-que-é-este-projeto)
- [O que você vai aprender aqui](#o-que-você-vai-aprender-aqui)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Rodando o projeto](#rodando-o-projeto)
- [Rodando os testes](#rodando-os-testes)
- [Endpoints da API](#endpoints-da-api)
- [Segurança e autenticação](#segurança-e-autenticação)
- [Padrões do projeto](#padrões-do-projeto)
- [Como contribuir](#como-contribuir)

---

## O que é este projeto?

A **Loja Online** é um sistema de e-commerce backend — sem interface gráfica, só a API REST que um aplicativo ou front-end consumiria. Ela gerencia usuários, produtos, endereços e autenticação.

O objetivo principal **não é só fazer o sistema funcionar** — é aprender como times reais de produto trabalham:

- escrever código organizado e testado
- colaborar via pull requests com code review
- usar ferramentas profissionais (CI, lint, Docker)
- evoluir o projeto com segurança, sem quebrar o que já existe

**Qualquer nível de experiência é bem-vindo.** Issues são categorizadas por nível para você escolher o desafio certo.

---

## O que você vai aprender aqui

| Área | O que você pratica |
|---|---|
| **API REST** | Criar endpoints, tratar erros, validar dados |
| **Spring Boot** | Controllers, Services, Repositories, injeção de dependência |
| **Banco de dados** | JPA, relacionamentos entre entidades, PostgreSQL |
| **Segurança** | Autenticação com JWT, controle de acesso por papel (Role) |
| **Testes** | Testes unitários com Mockito, testes de integração com Testcontainers |
| **Git & GitHub** | Branches, commits semânticos, pull requests, code review |
| **Docker** | Subir a aplicação e o banco em contêineres |
| **Qualidade** | Checkstyle, Spotless, pipelines de CI |

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JWT (jjwt) |
| Banco de dados | PostgreSQL 17 |
| ORM | Spring Data JPA / Hibernate |
| Containerização | Docker + Docker Compose |
| Documentação | Swagger / OpenAPI (springdoc) |
| Testes | JUnit 5 + Mockito + MockMvc + Testcontainers |
| Qualidade | Checkstyle + Spotless |
| Build | Maven (wrapper incluído — não precisa instalar Maven) |
| CI | GitHub Actions |

---

## Arquitetura

O projeto segue a arquitetura em camadas padrão do Spring Boot:

```
src/main/java/br/com/loja_online/
├── controller/     # Recebe as requisições HTTP e devolve as respostas
├── service/        # Onde ficam as regras de negócio
├── repository/     # Faz as consultas no banco de dados (Spring Data JPA)
├── model/          # As entidades — representam as tabelas do banco
├── dto/            # Objetos de entrada e saída das requisições
├── mapper/         # Converte entre entidades e DTOs
├── security/       # Autenticação JWT, filtros e configuração de segurança
├── exception/      # Tratamento global de erros (retorna JSON padronizado)
└── config/         # Configurações gerais (Swagger, CORS)
```

**Como uma requisição percorre o sistema:**

```
Cliente (Postman / Front-end)
        │
        ▼
   Controller          ← valida o formato da requisição (DTO + Bean Validation)
        │
        ▼
   Security Filter     ← verifica o token JWT (se a rota exige autenticação)
        │
        ▼
    Service            ← executa a regra de negócio (quem pode fazer o quê)
        │
        ▼
   Repository          ← consulta ou salva no banco de dados
        │
        ▼
   PostgreSQL
        │
   (resposta volta pelo mesmo caminho, convertida via Mapper → DTO)
```

**Entidades principais:**

```
Usuario ─── Login (credenciais de acesso)
   │
   ├── Endereco (pode ter vários)
   └── Carrinho
         └── ItemVenda ─── Produto
```

---

## Pré-requisitos

### Todos os caminhos

- [GNU Make](https://www.gnu.org/software/make/) — necessário para rodar os comandos do projeto (`make up`, `make lint`, etc.)

| SO | Como instalar |
|---|---|
| **Linux** | Já vem instalado na maioria das distros |
| **macOS** | `xcode-select --install` |
| **Windows** | `choco install make` (Chocolatey) ou `scoop install make` (Scoop) |

Escolha **um** dos dois caminhos:

### Caminho A — Docker (recomendado para começar)

Você não precisa instalar Java, Maven ou PostgreSQL. Só precisa do Docker.

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

### Caminho B — Ambiente local

Para rodar sem Docker (útil para desenvolvimento ativo):

- [JDK 21](https://adoptium.net/) instalado
- [PostgreSQL 17](https://www.postgresql.org/download/) instalado e rodando

---

## Rodando o projeto

### 1. Clone o repositório

```bash
git clone https://github.com/ProjetoLojaOnline/Loja_Online.git
cd Loja_Online
```

> **Usando Windows?** Os comandos abaixo usam `make`, que não vem instalado no Windows por padrão. Consulte a seção [Rodando no Windows](#rodando-no-windows-sem-make) para ver os comandos equivalentes.

### 2. Configure as variáveis de ambiente

O projeto usa um arquivo `.env` para guardar configurações sensíveis (senhas, secrets). **Nunca versione valores reais.**

```bash
cp envs/.env.example envs/.env.local
```

Abra `envs/.env.local` e preencha os campos obrigatórios (na verdade, a maioria já vem preenchida no template):

```env
DB_PASSWORD=uma_senha_qualquer     # deve ser igual à POSTGRES_PASSWORD
POSTGRES_PASSWORD=uma_senha_qualquer
JWT_SECRET=                        # gere com o comando abaixo
```

Para gerar o `JWT_SECRET`, rode um dos comandos abaixo no terminal e cole o resultado no `.env.local`:

```bash
# Linux / macOS
openssl rand -hex 32
```

```powershell
# Windows / Git Bash / PowerShell / WSL
-join ((1..32) | ForEach-Object { '{0:x2}' -f (Get-Random -Maximum 256) })
```

### 3. Configure os hooks do Git (uma vez só)

```bash
make setup-hooks
```

Isso ativa validação automática de mensagens de commit e proteção do branch `main`.

### 4. Suba a aplicação

```bash
make up
```

A aplicação estará disponível em `http://localhost:8080`.  
A documentação interativa (Swagger) estará em `http://localhost:8080/swagger-ui/index.html`.

### Outros comandos úteis

```bash
make down        # Para e remove os contêineres
make lint        # Formata o código automaticamente
make lint-check  # Verifica se o código está formatado (sem alterar)
```

### Rodando no Windows (sem make)

Se você está no Windows e não tem o `make` instalado, use os comandos equivalentes:

- **Setup hooks (uma vez só):**
  - Linux / macOS: `make setup-hooks`
  - Windows: `git config core.hooksPath .githooks`

- **Subir a aplicação:**
  - Linux / macOS: `make up`
  - Windows: `docker compose --env-file envs/.env.local up -d --wait`

- **Derrubar a aplicação:**
  - Linux / macOS: `make down`
  - Windows: `docker compose --env-file envs/.env.local down`

- **Formatar o código:**
  - Linux / macOS: `make lint`
  - Windows: `.\mvnw spotless:apply` + `.\mvnw checkstyle:check`

- **Verificar formatação (sem alterar):**
  - Linux / macOS: `make lint-check`
  - Windows: `.\mvnw spotless:check checkstyle:check`

> **Nota:** No Git Bash ou WSL, use `./mvnw` em vez de `.\mvnw`.

---

## Rodando os testes

O projeto tem dois tipos de testes:

| Tipo | O que testa | Comando |
|---|---|---|
| **Unitários** | Cada classe isolada (Mockito simula dependências) | `make test` |
| **Integração** | Fluxo completo com banco de dados real (Testcontainers sobe um PostgreSQL temporário) | `make test-integration` |
| **Todos** | Unitários + integração | `make test-all` |

> Os testes de integração sobem um banco PostgreSQL em um contêiner Docker automaticamente. Você precisa do Docker rodando, mas **não precisa ter o banco local configurado**.

### Rodando os testes no Windows (sem make)

| Comando | Linux / macOS | Windows (PowerShell) |
|---|---|---|
| **Unitários** | `make test` | `.\mvnw test` |
| **Integração** | `make test-integration` | `.\mvnw failsafe:integration-test failsafe:verify` |
| **Todos** | `make test-all` | `.\mvnw test` + `.\mvnw failsafe:integration-test failsafe:verify` |

> **Nota:** No Git Bash ou WSL, use `./mvnw` em vez de `.\mvnw`.

Para rodar os testes de integração em ambiente completo (sobe tudo via Docker Compose):

```bash
make test-docker
```

> No Windows sem make, rode manualmente os dois comandos acima na ordem (primeiro unitários, depois integração).

---

## Endpoints da API

A documentação completa e interativa está em `/swagger-ui/index.html` com o projeto rodando.

### Usuários

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/usuarios` | Pública | Cadastra novo usuário |
| `GET` | `/api/usuarios` | ADMIN | Lista todos os usuários |
| `GET` | `/api/usuarios/{id}` | Autenticado (próprio) | Busca usuário por ID |
| `GET` | `/api/usuarios/login/{login}` | Autenticado (próprio) | Busca usuário por login |
| `PUT` | `/api/usuarios/{id}` | Autenticado (próprio) | Atualiza dados do usuário |
| `DELETE` | `/api/usuarios/{id}` | Autenticado (próprio) | Remove o usuário |

### Autenticação

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/login/authenticate` | Pública | Autentica e retorna o token JWT |
| `GET` | `/login/buscar/{login}` | Autenticado | Busca credenciais por login |

### Produtos

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/produto` | Pública | Lista produtos (paginado) |
| `GET` | `/produto/{id}` | Pública | Busca produto por ID |
| `POST` | `/produto` | ADMIN ou VENDEDOR | Cadastra produto |
| `DELETE` | `/produto/{id}` | ADMIN ou VENDEDOR | Remove produto |

### Endereços

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/endereco/create` | Autenticado | Adiciona endereço |
| `GET` | `/endereco/{id}` | Autenticado (próprio) | Busca endereço por ID |
| `DELETE` | `/endereco/{id}` | Autenticado (próprio) | Remove endereço |

> **"Autenticado (próprio)"** significa que o usuário só pode acessar os próprios dados — tentar acessar dados de outro usuário retorna `403 Forbidden`.

---

## Segurança e autenticação

O projeto usa **JWT (JSON Web Token)** para autenticação stateless.

**Fluxo:**

```
1. POST /login/authenticate  → envia { "email": "...", "senha": "..." }
                                    ou { "username": "...", "senha": "..." }

2. Resposta: { "token": "eyJ..." }

3. Todas as requisições autenticadas enviam:
   Header: Authorization: Bearer eyJ...
```

**Papéis (Roles):**

| Role | O que pode fazer |
|---|---|
| `ROLE_USER` | Gerenciar os próprios dados (usuário, endereço) |
| `ROLE_VENDEDOR` | Tudo do USER + cadastrar e remover produtos |
| `ROLE_ADMIN` | Tudo do VENDEDOR + listar todos os usuários |

---

## Padrões do projeto

### Mensagens de commit (Conventional Commits)

Toda mensagem de commit deve seguir o padrão:

```
tipo(escopo opcional): descrição curta
```

| Tipo | Quando usar |
|---|---|
| `feat` | nova funcionalidade |
| `fix` | correção de bug |
| `refactor` | refatoração sem mudar comportamento externo |
| `test` | adição ou correção de testes |
| `chore` | manutenção (dependências, configuração, build) |
| `docs` | documentação |
| `ci` | pipelines e automação |
| `style` | formatação sem mudança de lógica |
| `perf` | melhoria de performance |

**Exemplos:**

```bash
git commit -m "feat(usuario): adiciona endpoint de busca por CPF"
git commit -m "fix: corrige validação de email no cadastro"
git commit -m "test(login): adiciona cenário de autenticação com senha inválida"
git commit -m "docs: atualiza README com novos endpoints"
```

O hook `commit-msg` avisa se a mensagem estiver fora do padrão.

### Branches e Pull Requests

O projeto usa o fluxo **trunk-based**: branches de curta duração criadas a partir de `main` e integradas de volta via Pull Request.

```
main  ←  feat/nome-da-funcionalidade
main  ←  fix/descricao-do-bug
main  ←  chore/o-que-foi-feito
```

**Regras:**

- Sempre crie sua branch a partir de `main` atualizado (`git pull origin main`)
- Uma branch = uma tarefa
- Abra um Pull Request quando a tarefa estiver pronta
- Nunca faça push direto em `main` — o hook `pre-push` bloqueia isso

### Variáveis de ambiente

- **Nunca versione** arquivos `.env.*` com valores reais — eles estão no `.gitignore`
- Use `envs/.env.example` como template (único arquivo de env no git)
- Perfis válidos: `local`, `dev`, `test` — **nunca `prod`** neste repositório

---

## Como contribuir

### Primeiros passos

1. Veja as [issues abertas](https://github.com/ProjetoLojaOnline/Loja_Online/issues) — elas têm labels de nível para facilitar a escolha
2. Comente na issue que deseja pegar para ninguém trabalhar em duplicata
3. Fork ou peça acesso ao repositório na comunidade **debug & grow**

### Escolha uma tarefa pelo seu nível

| Label | Para quem é |
|---|---|
| `iniciante` | Nunca contribuiu para um projeto open-source ou está aprendendo Java |
| `junior` | Conhece o básico de Java e Spring, quer praticar com mais autonomia |
| `pleno` | Confortável com Spring Boot, quer desafios de arquitetura e performance |

### Passo a passo

```bash
# 1. Atualize a main local
git checkout main
git pull origin main

# 2. Crie sua branch
git checkout -b feat/nome-da-funcionalidade

# 3. Configure os hooks (se ainda não fez)
make setup-hooks

# 4. Implemente e escreva os testes

# 5. Verifique lint e testes antes de commitar
make lint
make test

# 6. Commit e push
git add .
git commit -m "feat(escopo): descrição do que foi feito"
git push origin feat/nome-da-funcionalidade

# 7. Abra o Pull Request no GitHub descrevendo o que foi feito e por quê
```

> **Dúvidas?** Entre na comunidade **debug & grow** e pergunte sem cerimônia — o projeto existe para aprender junto.

---

<div align="center">
  Feito com dedicação pela comunidade <strong>debug & grow</strong>
</div>
