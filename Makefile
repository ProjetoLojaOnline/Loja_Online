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
## Falha  → mostra quais testes quebraram + docker down -v (ambiente limpo)
## Sucesso → docker down -v + git push automático
test-docker:
	@if [ ! -f envs/.env.test ]; then \
	  echo "❌ envs/.env.test não encontrado."; \
	  echo "   Crie com: cp envs/.env.example envs/.env.test"; \
	  exit 1; \
	fi
	@echo "🐳 Subindo ambiente de test..."
	@docker compose --env-file envs/.env.test up -d --wait \
	  || (docker compose --env-file envs/.env.test down -v; exit 1)
	@TEST_LOG=$$(mktemp /tmp/loja-test-XXXXXX.log); \
	EXIT_FILE=$$(mktemp); \
	echo "🔍 Rodando testes..."; \
	(./mvnw test --no-transfer-progress; echo $$? > $$EXIT_FILE) 2>&1 | tee $$TEST_LOG; \
	TEST_EXIT=$$(cat $$EXIT_FILE); rm -f $$EXIT_FILE; \
	echo ""; \
	echo "🧹 Derrubando ambiente de test..."; \
	docker compose --env-file envs/.env.test down -v; \
	if [ "$$TEST_EXIT" != "0" ]; then \
	  echo ""; \
	  echo "❌ Testes falharam — corrija antes de fazer push:"; \
	  echo ""; \
	  grep -e "<<< FAILURE" -e "<<< ERROR" $$TEST_LOG \
	    | sed 's/\[ERROR\] /  /' \
	    | sed 's/ Time elapsed.*<<< /  <<< /'; \
	  echo ""; \
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
