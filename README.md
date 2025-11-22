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
