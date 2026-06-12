# Loja Online — debug & grow

> Projeto open-source educacional da comunidade **debug & grow**  
> Foco em aprendizado de entrega de produto com qualidade profissional.

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Rodando com Docker](#rodando-com-docker)
- [Rodando os Testes](#rodando-os-testes)
- [Padrões do Projeto](#padrões-do-projeto)
- [Como Contribuir](#como-contribuir)
- [Endpoints da API](#endpoints-da-api)

---

## Sobre o Projeto

A **Loja Online** é um backend REST de e-commerce construído pela comunidade **debug & grow**.  
O objetivo não é apenas entregar um sistema funcional — é aprender, na prática, como times de produto trabalham:

- como organizar e versionar código profissionalmente
- como garantir qualidade com testes e lint automatizados
- como separar ambientes (local, dev, test) sem vazar configurações
- como colaborar em open-source com padrões reais de mercado

O projeto está em desenvolvimento ativo. Novos colaboradores são bem-vindos em qualquer nível de experiência.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Banco de dados | PostgreSQL 17 |
| Containerização | Docker + Docker Compose |
| Documentação | Swagger / OpenAPI (springdoc) |
| Qualidade de código | Checkstyle + Spotless |
| Testes | JUnit 5 + Mockito + MockMvc |
| Build | Maven (wrapper incluído) |

---

## Arquitetura

```
src/
└── main/java/br/com/loja_online/
    ├── controller/     # Camada HTTP — recebe e responde requisições
    ├── service/        # Regras de negócio
    ├── repository/     # Acesso ao banco de dados (Spring Data JPA)
    ├── model/          # Entidades JPA (tabelas do banco)
    ├── dto/            # Objetos de transferência de dados (request/response)
    ├── mapper/         # Conversão entre entidades e DTOs
    └── exception/      # Tratamento global de erros
```

Fluxo de uma requisição:

```
Cliente → Controller → Service → Repository → PostgreSQL
                ↑                       ↓
               DTO ←── Mapper ←── Entidade
```

---

## Pré-requisitos

Você só precisa de um dos dois caminhos abaixo:

**Caminho A — Docker (recomendado para iniciantes)**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

**Caminho B — local sem Docker**
- Java 21+
- Maven 3.9+
- PostgreSQL 17 rodando localmente

---

## Rodando com Docker

### 1. Clone o repositório

```bash
git clone git@github.com:ProjetoLojaOnline/Loja_Online.git
cd Loja_Online
```

### 2. Configure os git hooks (uma única vez após clonar)

```bash
make setup-hooks
```

Isso ativa a validação de formatação e testes antes de cada push.

### 3. Crie seu arquivo de ambiente local

```bash
cp envs/.env.example envs/.env.local
```

Abra `envs/.env.local` e defina uma senha para `DB_PASSWORD` e `POSTGRES_PASSWORD`.  
Esse arquivo é pessoal — **nunca** será versionado no git.

### 4. Suba o projeto

```bash
docker compose --env-file envs/.env.local up --build
```

Aguarde até ver a mensagem `Started LojaOnlineApplication` no terminal.

### 5. Acesse

| Recurso | URL |
|---|---|
| API | http://localhost:8080/api/usuarios |
| Documentação interativa (Swagger) | http://localhost:8080/swagger-ui/index.html |
| Health check | http://localhost:8080/actuator/health |

### Parar o projeto

```bash
docker compose --env-file envs/.env.local down
```

Para limpar volumes (apaga o banco):

```bash
docker compose --env-file envs/.env.local down -v
```

---

## Rodando os Testes

### Testes unitários (sem Docker)

```bash
make test
# ou: ./mvnw test
```

### Testes com ambiente Docker completo

Crie o arquivo de ambiente de test (uma única vez):

```bash
cp envs/.env.example envs/.env.test
# Edite envs/.env.test com credenciais de test (porta 5433 para não conflitar com local)
```

Execute:

```bash
make test-docker
```

O que acontece automaticamente:
1. Sobe o banco PostgreSQL de test
2. Roda todos os testes
3. Se falhar → mostra exatamente quais testes quebraram e derruba o ambiente
4. Se passar → derruba o ambiente e faz `git push`

---

## Padrões do Projeto

Esses padrões são cobrados nos PRs. Configure os hooks com `make setup-hooks` para ser avisado localmente antes de enviar.

### Formatação de código

```bash
make lint        # corrige automaticamente imports, espaços, quebras de linha
make lint-check  # só verifica (sem alterar) — usado pelo CI
```

O projeto usa **Spotless** para formatação e **Checkstyle** para regras de estilo.  
O pre-push hook roda `lint-check` + testes automaticamente. Se não passar, o push é bloqueado.

### Mensagens de commit — Conventional Commits

Todo commit deve seguir o formato:

```
<tipo>(escopo opcional): descrição curta em minúsculas
```

| Tipo | Quando usar |
|---|---|
| `feat` | nova funcionalidade |
| `fix` | correção de bug |
| `refactor` | refatoração sem mudar comportamento |
| `test` | adição ou correção de testes |
| `chore` | manutenção (deps, configuração, build) |
| `docs` | documentação |
| `ci` | pipelines e automação |
| `style` | formatação sem mudança de lógica |
| `perf` | melhoria de performance |
| `revert` | reverte um commit anterior |

**Exemplos:**

```bash
git commit -m "feat(usuario): adiciona endpoint de busca por CPF"
git commit -m "fix: corrige NPE ao salvar cartão sem usuário associado"
git commit -m "chore: atualiza dependências para versão LTS"
git commit -m "test(login): adiciona cenário de autenticação com credencial inválida"
git commit -m "feat!: altera contrato do endpoint de login (breaking change)"
```

O hook `commit-msg` avisa se a mensagem estiver fora do padrão — o commit ainda é realizado, mas vale ajustar antes de abrir o PR.

### Branches e Pull Requests

O projeto usa um fluxo hierárquico de branches:

```
main  ←  develop  ←  epic/nome  ←  feat/nome-da-task
```

| Branch | Criada a partir de | Merge para |
|---|---|---|
| `develop` | `main` | `main` (quando estável) |
| `epic/nome` | `develop` | `develop` (quando o épico estiver completo) |
| `feat/nome`, `fix/nome`, `chore/nome` | `epic/nome` | `epic/nome` (ao concluir a task) |

**Regras:**

- Nunca crie branch diretamente a partir de `main` (exceto `develop`)
- Cada task vira uma branch separada dentro do seu épico
- PRs de task → épico, PRs de épico → `develop`, PRs de `develop` → `main`
- `main` só recebe código estável e validado em `develop`

### Variáveis de ambiente e segredos

- **Nunca versione** arquivos `.env.*` com valores reais — eles estão no `.gitignore`
- Use `envs/.env.example` como template (o único arquivo de env no git)
- Perfis válidos: `local`, `dev`, `test` — **nunca `prod`** neste repositório

---

## Como Contribuir

1. Verifique as [issues abertas](https://github.com/ProjetoLojaOnline/Loja_Online/issues) e comente na que deseja pegar
2. Fork ou peça acesso ao repositório na comunidade **debug & grow**
3. Crie uma branch a partir de `main` com o padrão:

```bash
git checkout -b feat/nome-da-funcionalidade
git checkout -b fix/descricao-do-bug
git checkout -b chore/o-que-foi-feito
```

4. Configure os hooks:

```bash
make setup-hooks
```

5. Implemente, escreva testes e rode:

```bash
make lint   # formata o código
make test   # valida que nada quebrou
```

6. Abra um Pull Request descrevendo o que foi feito e por quê.

> **Dúvidas?** Entre na comunidade **debug & grow** e pergunte sem cerimônia — o projeto existe para aprender junto.

---

## Endpoints da API

A documentação completa e interativa está em `/swagger-ui/index.html` com o projeto rodando.  
Visão geral dos recursos disponíveis:

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/usuarios` | Lista todos os usuários |
| GET | `/api/usuarios/{id}` | Busca usuário por ID |
| GET | `/api/usuarios/login/{login}` | Busca usuário por login |
| POST | `/api/usuarios` | Cadastra novo usuário |
| PUT | `/api/usuarios/{id}` | Atualiza dados do usuário |
| DELETE | `/api/usuarios/{id}` | Remove usuário |
| GET | `/login/buscar/{login}` | Busca credenciais por login |
| POST | `/login/authenticate` | Autentica usuário |
| GET | `/produto` | Lista produtos (paginado) |
| GET | `/produto/{id}` | Busca produto por ID |
| POST | `/produto` | Cadastra produto |
| PUT | `/produto/{id}` | Atualiza produto |
| DELETE | `/produto/{id}` | Remove produto |
| GET | `/cartao` | Lista cartões |
| POST | `/cartao` | Adiciona cartão |
| PATCH | `/cartao/{id}` | Atualiza cartão |
| DELETE | `/cartao/{id}` | Remove cartão |
| POST | `/endereco` | Adiciona endereço |
| GET | `/endereco/{id}` | Busca endereço por ID |
| PUT | `/endereco/{id}` | Atualiza endereço |
| DELETE | `/endereco/{id}` | Remove endereço |

---

<div align="center">
  Feito com dedicação pela comunidade <strong>debug & grow</strong>
</div>
