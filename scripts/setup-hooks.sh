#!/bin/sh
# Configura o git para usar os hooks da pasta .githooks/
# Execute uma vez após clonar o repositório: sh scripts/setup-hooks.sh

git config core.hooksPath .githooks
echo "✅ Git hooks configurados. O push será bloqueado se os testes falharem."
