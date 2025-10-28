#!/bin/bash
# Script para inicializar Git e preparar primeiro commit

set -e

echo "================================================"
echo "🚀 Inicializando Git para MCP Server"
echo "================================================"
echo ""

# Verificar se já é um repositório Git
if [ -d .git ]; then
    echo "⚠️  Este diretório já é um repositório Git."
    read -p "Deseja continuar? (s/N): " confirm
    if [[ ! "$confirm" =~ ^[Ss]$ ]]; then
        echo "Operação cancelada."
        exit 0
    fi
else
    echo "📁 Inicializando repositório Git..."
    git init
    echo "✅ Repositório inicializado"
fi

echo ""
echo "📋 Configurando usuário Git..."
echo "   (Você pode mudar depois com 'git config')"
echo ""

# Verificar se usuário já está configurado
USER_NAME=$(git config user.name 2>/dev/null || echo "")
USER_EMAIL=$(git config user.email 2>/dev/null || echo "")

if [ -z "$USER_NAME" ]; then
    read -p "Nome: " git_name
    git config user.name "$git_name"
else
    echo "Nome: $USER_NAME (já configurado)"
fi

if [ -z "$USER_EMAIL" ]; then
    read -p "Email: " git_email
    git config user.email "$git_email"
else
    echo "Email: $USER_EMAIL (já configurado)"
fi

echo ""
echo "📝 Adicionando arquivos ao stage..."
git add .

echo ""
echo "💾 Criando commit inicial..."
git commit -m "chore: inicialização do projeto MCP Server

- 16 classes Java implementadas
- 25 testes automatizados (cobertura ~82%)
- 4 ferramentas MCP (JSON-RPC 2.0)
- Suporte multi-banco (PostgreSQL, MySQL, Oracle, MSSQL)
- Documentação completa (8 arquivos .md)
- CI/CD com GitHub Actions
- Automação de releases

Componentes:
- Controller: McpController (JSON-RPC 2.0)
- Services: Schema, TableDetails, Trigger, SecureQuery
- Tools: McpToolsRegistry (4 ferramentas)
- DTOs: SchemaStructure, TableDetails, TriggerList, QueryResult
- Config: Caching, MCP Server settings
- Tests: 3 suites (25 testes, cobertura 82%)

Documentação:
- README.md - Documentação principal
- QUICKSTART.md - Guia rápido
- IMPLEMENTATION_SUMMARY.md - Resumo técnico
- PROJECT_STATUS.md - Status do projeto
- TESTING_GUIDE.md - Guia de testes
- TOKENIZATION_GUIDE.md - Guia de tokenização
- COVERAGE_REPORT.md - Relatório de cobertura
- RELEASE_HISTORY.md - Histórico de releases
- EXECUTAR_TESTES.md - Como executar testes
- COMO_FAZER_RELEASE.md - Como fazer releases

CI/CD:
- .github/workflows/ci.yml - Testes em todo commit
- .github/workflows/release.yml - Release automático (tag REL-*)

Scripts:
- gerar-relatorio-cobertura.sh - Gera relatório JaCoCo
- criar-release.sh - Cria release manualmente
- test-mcp-server.sh - Testa servidor (Bash)
- mcp_client_example.py - Cliente exemplo (Python)
"

echo ""
echo "================================================"
echo "✅ Git inicializado com sucesso!"
echo "================================================"
echo ""
echo "📊 Status do repositório:"
git log --oneline -1
echo ""
echo "🔗 Próximos passos:"
echo ""
echo "1️⃣  Criar repositório no GitHub:"
echo "    https://github.com/new"
echo ""
echo "2️⃣  Adicionar remote origin:"
echo "    git remote add origin https://github.com/SEU_USUARIO/aiToSql.git"
echo ""
echo "3️⃣  Renomear branch para main (se necessário):"
echo "    git branch -M main"
echo ""
echo "4️⃣  Fazer push inicial:"
echo "    git push -u origin main"
echo ""
echo "5️⃣  Criar primeira release:"
echo "    git tag -a REL-0.0.1 -m 'Primeira release'"
echo "    git push origin REL-0.0.1"
echo ""
echo "================================================"
