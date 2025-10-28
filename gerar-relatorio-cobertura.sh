#!/bin/bash
# Script para gerar relatório de cobertura de testes

set -e

echo "================================================"
echo "🧪 Gerando Relatório de Cobertura de Testes"
echo "================================================"
echo ""

cd "$(dirname "$0")"

echo "📋 Executando testes com JaCoCo..."
mvn clean test jacoco:report

echo ""
echo "================================================"
echo "✅ Relatório Gerado com Sucesso!"
echo "================================================"
echo ""
echo "📊 Abrir relatório HTML:"
echo "   firefox target/site/jacoco/index.html"
echo "   google-chrome target/site/jacoco/index.html"
echo "   xdg-open target/site/jacoco/index.html"
echo ""
echo "📁 Localização dos arquivos:"
echo "   HTML: target/site/jacoco/index.html"
echo "   XML:  target/site/jacoco/jacoco.xml"
echo "   CSV:  target/site/jacoco/jacoco.csv"
echo ""
echo "================================================"
