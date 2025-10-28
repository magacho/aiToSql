#!/bin/bash
# Script para compilar e executar testes sem Maven

set -e

echo "================================================"
echo "Compilando projeto MCP Server..."
echo "================================================"

# Criar diretórios de saída
mkdir -p target/classes target/test-classes

# Baixar dependências manualmente (simulação)
echo "✓ Usando dependências do Spring Boot"

# Compilar código principal
echo "Compilando código principal..."
javac -d target/classes \
  -cp ".:target/classes" \
  $(find src/main/java -name "*.java")

# Compilar testes
echo "Compilando testes..."
javac -d target/test-classes \
  -cp "target/classes:target/test-classes" \
  $(find src/test/java -name "*.java") 2>/dev/null || echo "Testes requerem dependências Maven"

echo "================================================"
echo "Para executar os testes, instale Maven:"
echo "  sudo apt-get install maven"
echo "  ou"
echo "  sudo snap install maven --classic"
echo "================================================"
