#!/bin/bash
# Script para criar release com histórico de cobertura

set -e

echo "================================================"
echo "🚀 Criando Release com Histórico de Cobertura"
echo "================================================"
echo ""

# Verificar se há mudanças não commitadas
if [[ -n $(git status -s) ]]; then
    echo "⚠️  Existem mudanças não commitadas!"
    echo "   Commit ou stash antes de criar release."
    exit 1
fi

# Solicitar versão
echo "📋 Versão atual: $(grep -oP '<version>\K[^<]+' pom.xml | head -1)"
read -p "Nova versão (ex: 1.0.0): " VERSION

if [[ -z "$VERSION" ]]; then
    echo "❌ Versão não pode ser vazia!"
    exit 1
fi

echo ""
echo "🧪 Executando testes..."
mvn clean test

echo ""
echo "📊 Gerando relatório de cobertura..."
mvn jacoco:report

echo ""
echo "📈 Extraindo métricas..."

# Extrair cobertura do relatório
COVERAGE=$(awk -F',' '{ instructions += $4 + $5; covered += $5 } END { printf "%.1f", 100*covered/instructions }' target/site/jacoco/jacoco.csv)

echo "   Cobertura Total: $COVERAGE%"

# Contar testes
TESTS=$(mvn -q test | grep -oP 'Tests run: \K\d+' | head -1)
echo "   Total de Testes: $TESTS"

# Atualizar versão no pom.xml
echo ""
echo "📝 Atualizando versão no pom.xml..."
sed -i "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$VERSION<\/version>/" pom.xml

# Atualizar RELEASE_HISTORY.md
echo ""
echo "📝 Atualizando RELEASE_HISTORY.md..."

DATE=$(date +"%d de %B de %Y")
RELEASE_SECTION="## 📋 Versão $VERSION

**Data**: $DATE  
**Status**: 🚀 Produção

### 📊 Cobertura de Testes

| Métrica | Valor | Status |
|---------|-------|--------|
| **Cobertura Total** | $COVERAGE% | $(if (( $(echo "$COVERAGE >= 80" | bc -l) )); then echo "✅"; else echo "⚠️"; fi) |
| **Total de Testes** | $TESTS | ✅ |

### ✨ Novas Funcionalidades

- (Adicionar manualmente)

### 🔧 Correções

- (Adicionar manualmente)

---

"

# Inserir após o template
sed -i "/## 📋 Template para Próximas Releases/i $RELEASE_SECTION" RELEASE_HISTORY.md

# Comprimir relatório JaCoCo
echo ""
echo "📦 Comprimindo relatório JaCoCo..."
cd target/site
zip -r jacoco-v$VERSION.zip jacoco/
cd ../..

# Commit e tag
echo ""
echo "💾 Criando commit e tag..."
git add pom.xml RELEASE_HISTORY.md
git commit -m "chore: release v$VERSION - Coverage: $COVERAGE%"
git tag -a "v$VERSION" -m "Release v$VERSION

Cobertura de Testes: $COVERAGE%
Total de Testes: $TESTS

Relatório completo: target/site/jacoco-v$VERSION.zip"

echo ""
echo "================================================"
echo "✅ Release v$VERSION criada com sucesso!"
echo "================================================"
echo ""
echo "📊 Métricas:"
echo "   • Cobertura: $COVERAGE%"
echo "   • Testes: $TESTS"
echo ""
echo "🔄 Próximos passos:"
echo "   1. Revisar RELEASE_HISTORY.md"
echo "   2. Adicionar funcionalidades e correções"
echo "   3. git push origin main"
echo "   4. git push origin v$VERSION"
echo ""
echo "📦 Relatório de cobertura:"
echo "   target/site/jacoco-v$VERSION.zip"
echo ""
