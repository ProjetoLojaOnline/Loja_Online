.PHONY: lint lint-check test setup-hooks

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

## Roda os testes
test:
	@./mvnw test --no-transfer-progress

## Configura os git hooks (rode uma vez após clonar)
setup-hooks:
	@git config core.hooksPath .githooks
	@echo "✅ Git hooks configurados."
