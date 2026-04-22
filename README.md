# Loja Online

## 📦 Sobre o Projeto

- Este é um projeto open-source de uma aplicação backend para uma loja online, utilizando Java 17 com Spring Boot
- Ainda está em deesenvolvimento, mas já oferece funcionalidades básicas para gerenciar produtos.

---

## 🐳 Rodando o Projeto com Docker

Se você não tem o Java e o Mysql instalados, você pode usar o Docker.

### 📥 1. Clone o repositório

```
git clone https://github.com/ProjetoLojaOnline/Loja_Online
```

### 📂 2. Entre na pasta do projeto

```
cd Loja_Online
```

### ▶️ 3. Abra o Docker Desktop e suba tudo com esse comando:

```
docker compose up --build --force-recreate
```

Isso vai:

* subir o backend em Spring Boot na porta 8080
* subir o Mysql na porta 3306
* criar automaticamente o schema do banco

---

### 🌐 4. Acessar o CRUD de Produtos

Depois do container subir, basta abrir no navegador:

```
http://localhost:8080/crud/produto.html
```

Esta página é um front-end básico que executa:

- ✔ **Listagem produtos** (GET /produto)
- ✔ **Busca de produto por ID** (GET /produto/{id})
- ✔ **Criação de produto** (POST /produto)
- ✔ **Exclusão de produto** (DELETE /produto/{id})
- ❌ **Atualização de produto (PUT)** — *ainda não implementado*

---

## 🧪 Executando os Testes

O projeto conta com testes automatizados para garantir a estabilidade do sistema. A suíte de testes contempla:
- **Testes Unitários**: Validam pequenas unidades do código de forma isolada (como services e regras matemáticas).
- **Testes de Integração**: Testam a conexão de componentes, como as rotas da API integradas ao banco de dados. Utilizamos um banco em memória (H2) para que rodem de forma independente e não modifiquem o banco de dados principal (MySQL), evitando necessitar que o Docker esteja rodando.

### 1. Execute os testes (Unitários e de Integração)
Para rodar ambas as suítes de teste de uma única vez, basta executar o seguinte comando utilizando o Maven Wrapper na raiz do projeto:

```bash
./mvnw test
```

### ❓ Por que testar a aplicação?
* **Garantia de Qualidade:** Verifica automaticamente se as regras de negócio cumprem o que prometem.
* **Prevenção de Regressões (Quebras):** Se você alterar ou adicionar uma funcionalidade hoje, os testes avisam se a alteração bagunçou o que já funcionava.
* **Segurança na Refatoração:** Te dá confiança para reestruturar e otimizar o projeto sem medo de o sistema parar de funcionar.

---
