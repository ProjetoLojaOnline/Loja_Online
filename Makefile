.PHONY: lint lint-check test test-integration test-all test-docker up down setup-hooks

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

## Sobe o ambiente local (banco + app)
up:
	@if [ ! -f envs/.env.local ]; then \
	  echo "❌ envs/.env.local não encontrado."; \
	  echo "   Crie com: cp envs/.env.example envs/.env.local"; \
	  exit 1; \
	fi
	@docker compose --env-file envs/.env.local up -d --wait

## Derruba o ambiente local
down:
	@docker compose --env-file envs/.env.local down

## Roda os testes unitários (sem Docker)
test:
	@./mvnw test --no-transfer-progress

## Roda os testes de integração (Testcontainers — requer Docker)
test-integration:
	@./mvnw failsafe:integration-test failsafe:verify --no-transfer-progress

## Roda todos os testes: unitários + integração (requer Docker)
test-all:
	@./mvnw test --no-transfer-progress
	@./mvnw failsafe:integration-test failsafe:verify --no-transfer-progress

## Roda todos os testes e faz push automático ao final.
## Falha → mostra quais testes quebraram. Sucesso → git push.
test-docker:
	@TEST_LOG=$$(mktemp /tmp/loja-test-XXXXXX.log); \
	EXIT_FILE=$$(mktemp); \
	echo "🔍 Rodando testes unitários..."; \
	(./mvnw test --no-transfer-progress; echo $$? > $$EXIT_FILE) 2>&1 | tee $$TEST_LOG; \
	UNIT_EXIT=$$(cat $$EXIT_FILE); rm -f $$EXIT_FILE; \
	if [ "$$UNIT_EXIT" != "0" ]; then \
	  echo "❌ Testes unitários falharam — corrija antes de fazer push:"; \
	  grep -e "<<< FAILURE" -e "<<< ERROR" $$TEST_LOG \
	    | sed 's/\[ERROR\] /  /' | sed 's/ Time elapsed.*<<< /  <<< /'; \
	  echo "   Log completo: $$TEST_LOG"; \
	  exit 1; \
	fi; \
	echo ""; \
	echo "🔍 Rodando testes de integração (Testcontainers)..."; \
	(./mvnw failsafe:integration-test failsafe:verify --no-transfer-progress; echo $$? > $$EXIT_FILE) 2>&1 | tee -a $$TEST_LOG; \
	IT_EXIT=$$(cat $$EXIT_FILE); rm -f $$EXIT_FILE; \
	if [ "$$IT_EXIT" != "0" ]; then \
	  echo "❌ Testes de integração falharam — corrija antes de fazer push:"; \
	  grep -e "<<< FAILURE" -e "<<< ERROR" $$TEST_LOG \
	    | sed 's/\[ERROR\] /  /' | sed 's/ Time elapsed.*<<< /  <<< /'; \
	  echo "   Log completo: $$TEST_LOG"; \
	  exit 1; \
	fi; \
	rm -f $$TEST_LOG; \
	echo ""; \
	echo "✅ Todos os testes passaram!"; \
	echo "🚀 Fazendo push..."; \
	git push

## Configura os git hooks (rode uma vez após clonar)
setup-hooks:
	@git config core.hooksPath .githooks
	@echo "✅ Git hooks configurados."
