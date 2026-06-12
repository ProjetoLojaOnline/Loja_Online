.PHONY: lint lint-check test test-docker setup-hooks

## Corrige automaticamente: formatação, imports, trailing whitespace
lint:
	@echo "🔧 Aplicando formatação automática..."
	@./mvnw spotless:apply --no-transfer-progress -q
	@echo "✅ Formatação aplicada!"
	@echo ""
	@echo "🔍 Verificando padrões restantes (Checkstyle)..."
	@./mvnw checkstyle:check --no-transfer-progress -q && echo "✅ Checkstyle OK!" || \
	  (echo "" && \
	   echo "❌ Checkstyle encontrou violações que precisam de correção manual." && \
	   echo "   Execute: ./mvnw checkstyle:check  para ver os detalhes." && \
	   exit 1)

## Apenas verifica — não altera nenhum arquivo (usado pelo CI e pre-push)
lint-check:
	@./mvnw spotless:check checkstyle:check --no-transfer-progress -q

## Roda os testes unitários (sem Docker)
test:
	@./mvnw test --no-transfer-progress

## Sobe o ambiente de test, roda os testes e derruba tudo (limpo)
test-docker:
	@if [ ! -f envs/.env.test ]; then \
	  echo "❌ envs/.env.test não encontrado."; \
	  echo "   Crie com: cp envs/.env.example envs/.env.test"; \
	  exit 1; \
	fi
	@echo "🐳 Subindo ambiente de test..."
	@docker compose --env-file envs/.env.test up -d --wait \
	  || (docker compose --env-file envs/.env.test down -v && exit 1)
	@echo "🔍 Rodando testes..."; \
	./mvnw test --no-transfer-progress; TEST_EXIT=$$?; \
	echo "🧹 Derrubando ambiente de test..."; \
	docker compose --env-file envs/.env.test down -v; \
	exit $$TEST_EXIT

## Configura os git hooks (rode uma vez após clonar)
setup-hooks:
	@git config core.hooksPath .githooks
	@echo "✅ Git hooks configurados."
