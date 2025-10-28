# Como Fazer uma Release - Guia Completo

## 🎯 Visão Geral

O projeto possui **automação completa de releases**. Quando você cria uma tag com o padrão `REL-X.X.X`, o GitHub Actions automaticamente:

1. ✅ Executa todos os testes
2. ✅ Gera relatório de cobertura
3. ✅ Cria pacote JAR
4. ✅ Atualiza RELEASE_HISTORY.md
5. ✅ Cria GitHub Release
6. ✅ Anexa artefatos (JAR + relatório)

---

## 🚀 Processo de Release (3 passos)

### Passo 1: Preparar o Código

```bash
# 1. Garantir que está na branch main
git checkout main
git pull origin main

# 2. Verificar que tudo funciona
mvn clean test

# 3. Verificar cobertura
mvn jacoco:report
firefox target/site/jacoco/index.html
```

### Passo 2: Criar Tag de Release

```bash
# Formato: REL-MAJOR.MINOR.PATCH
# Exemplos:
#   REL-0.0.1  (primeira versão)
#   REL-1.0.0  (primeira versão de produção)
#   REL-1.1.0  (nova funcionalidade)
#   REL-1.1.1  (correção de bug)

# Criar tag local
git tag -a REL-1.0.0 -m "Release 1.0.0 - Primeira versão de produção"

# Enviar tag para GitHub (ISSO DISPARA A AUTOMAÇÃO!)
git push origin REL-1.0.0
```

### Passo 3: Acompanhar Automação

```bash
# Ver progresso no GitHub Actions
# https://github.com/seu-usuario/aiToSql/actions

# Ou via CLI:
gh run watch
```

**A automação levará ~3-5 minutos** e criará:
- ✅ Release no GitHub
- ✅ JAR da aplicação
- ✅ Relatório de cobertura
- ✅ Release notes automáticas
- ✅ RELEASE_HISTORY.md atualizado

---

## 🔄 Fluxo Completo

```
Developer                    GitHub Actions               GitHub Release
    │                              │                            │
    ├─ git tag REL-1.0.0          │                            │
    │                              │                            │
    ├─ git push origin REL-1.0.0 ─→ Workflow Disparado        │
    │                              │                            │
    │                              ├─ Checkout código          │
    │                              ├─ Setup JDK 21             │
    │                              ├─ Executar testes          │
    │                              ├─ Gerar cobertura          │
    │                              ├─ Extrair métricas         │
    │                              ├─ Validar cobertura ≥80%   │
    │                              ├─ Empacotar JAR            │
    │                              ├─ Comprimir relatório      │
    │                              ├─ Atualizar RELEASE_HISTORY│
    │                              ├─ Gerar release notes      │
    │                              ├─ Criar GitHub Release    ─→ Release Publicado
    │                              ├─ Upload artefatos        ─→ JAR + Cobertura
    │                              ├─ Commit RELEASE_HISTORY   │
    │                              └─ Upload Codecov           │
    │                              │                            │
    │                         ✅ Sucesso!                       │
    │                              │                            │
    ├─ Verificar release ─────────────────────────────────────→ Download JAR
```

---

## 📋 Exemplo Prático

### Release 1.0.0 (Primeira Versão de Produção)

```bash
# 1. Verificar status
git status

# 2. Criar tag
git tag -a REL-1.0.0 -m "Release 1.0.0

Primeira versão de produção com:
- Protocolo JSON-RPC 2.0 completo
- 4 ferramentas MCP
- Cobertura de 82%
- 25 testes automatizados"

# 3. Push da tag (dispara automação)
git push origin REL-1.0.0

# 4. Acompanhar (opcional)
gh run watch

# 5. Após 3-5 minutos, verificar release
gh release view REL-1.0.0
```

### Release 1.1.0 (Nova Funcionalidade)

```bash
git tag -a REL-1.1.0 -m "Release 1.1.0 - Nova ferramenta de análise"
git push origin REL-1.1.0
```

### Release 1.1.1 (Correção de Bug)

```bash
git tag -a REL-1.1.1 -m "Release 1.1.1 - Correção de segurança"
git push origin REL-1.1.1
```

---

## 🔍 O Que o GitHub Actions Faz

### 1. Execução de Testes (2-3 min)

```
🧪 Executando suite completa de testes...
  ✓ SecureQueryServiceTest: 13 testes
  ✓ McpControllerTest: 7 testes
  ✓ McpToolsRegistryTest: 5 testes
  
✅ 25 testes executados com sucesso
```

### 2. Geração de Cobertura (30 seg)

```
📊 Gerando relatório de cobertura...
  ✓ Cobertura Total: 82.5%
  ✓ Linhas cobertas: 1,234 / 1,500
  ✓ Branches cobertos: 156 / 200
  
✅ Meta de 80% atingida!
```

### 3. Validações (10 seg)

```
✓ Todos os testes passaram
✓ Cobertura ≥ 80%
✓ Sem erros de compilação
✓ Código pronto para produção
```

### 4. Criação de Artefatos (1 min)

```
📦 Criando artefatos...
  ✓ aiToSql-1.0.0.jar (12 MB)
  ✓ jacoco-1.0.0.zip (850 KB)
  
✅ Artefatos prontos para upload
```

### 5. Atualização de Documentação (30 seg)

```
📝 Atualizando RELEASE_HISTORY.md...
  ✓ Adicionada versão 1.0.0
  ✓ Incluída cobertura: 82.5%
  ✓ Listados 25 testes
  
✅ Documentação atualizada
```

### 6. Criação da Release (1 min)

```
🚀 Criando GitHub Release...
  ✓ Release v1.0.0 criada
  ✓ Release notes geradas
  ✓ JAR anexado
  ✓ Relatório de cobertura anexado
  
✅ Release publicada!
```

---

## 📊 Release Notes Automáticas

O sistema gera automaticamente:

```markdown
# Release v1.0.0

## 📊 Métricas de Qualidade

- **Cobertura de Testes**: 82.5%
- **Total de Testes**: 25
- **Taxa de Sucesso**: 100%

## 🎯 Destaques

- ✅ Protocolo JSON-RPC 2.0 completo
- ✅ 4 ferramentas MCP implementadas
- ✅ Suporte multi-banco
- ✅ Segurança SQL rigorosa

## 📦 Artefatos Incluídos

- aiToSql-1.0.0.jar
- jacoco-1.0.0.zip
```

---

## 🛡️ Segurança e Validações

### ❌ Release é CANCELADA se:

1. **Testes falham**
   ```
   ❌ Erro: 3 testes falharam
   Release cancelado automaticamente
   ```

2. **Erro de compilação**
   ```
   ❌ Erro: Código não compila
   Release cancelado automaticamente
   ```

### ⚠️ Alerta se:

1. **Cobertura abaixo de 80%**
   ```
   ⚠️ ATENÇÃO: Cobertura em 75%
   Considere adicionar testes
   Release continua, mas com aviso
   ```

---

## 📈 RELEASE_HISTORY.md Atualizado

A automação adiciona automaticamente:

```markdown
## 📋 Versão 1.0.0

**Data**: 28 de Outubro de 2024  
**Status**: 🚀 Produção

### 📊 Cobertura de Testes

| Métrica | Valor | Status |
|---------|-------|--------|
| **Cobertura Total** | 82.5% | 🟢 |
| **Total de Testes** | 25 | ✅ |

### 📦 Artefatos

- JAR: aiToSql-1.0.0.jar
- Relatório: jacoco-1.0.0.zip
```

---

## 🔄 Testes em Todo Commit

### CI Contínuo (workflow: ci.yml)

```yaml
on:
  push:
    branches: [ '**' ]  # TODOS os branches
  pull_request:
    branches: [ '**' ]
```

**Executado em:**
- ✅ Todo push
- ✅ Todo pull request
- ✅ Qualquer branch

**Executa:**
- 🧪 Todos os testes
- 📊 Relatório de cobertura
- 💬 Comentário no PR com métricas
- 📤 Upload de artifacts

---

## 📝 Comandos Úteis

### Ver Releases

```bash
# Listar todas as tags
git tag -l

# Listar releases do GitHub
gh release list

# Ver detalhes de uma release
gh release view REL-1.0.0
```

### Download de Artefatos

```bash
# Download do JAR
gh release download REL-1.0.0 -p "*.jar"

# Download do relatório de cobertura
gh release download REL-1.0.0 -p "jacoco-*.zip"
```

### Deletar Release (se necessário)

```bash
# Deletar release no GitHub
gh release delete REL-1.0.0

# Deletar tag local
git tag -d REL-1.0.0

# Deletar tag remota
git push origin --delete REL-1.0.0
```

---

## 🎯 Checklist Pré-Release

Antes de criar a tag REL-*, verifique:

- [ ] ✅ Código compila: `mvn clean compile`
- [ ] ✅ Testes passam: `mvn test`
- [ ] ✅ Cobertura ≥ 80%: `mvn jacoco:report`
- [ ] ✅ Sem TODOs críticos no código
- [ ] ✅ Documentação atualizada
- [ ] ✅ RELEASE_HISTORY.md revisado
- [ ] ✅ Commit de todas as mudanças
- [ ] ✅ Branch main atualizada

---

## 🚨 Troubleshooting

### Problema: Release não foi criada

**Causa**: Tag não segue padrão `REL-*`

**Solução**:
```bash
# Criar tag com nome correto
git tag -a REL-1.0.0 -m "Release 1.0.0"
git push origin REL-1.0.0
```

### Problema: Testes falharam na automação

**Causa**: Testes não passam no ambiente CI

**Solução**:
```bash
# Executar testes localmente primeiro
mvn clean test

# Se passar local mas falhar no CI, verificar:
# - Dependências
# - Variáveis de ambiente
# - Versão do Java
```

### Problema: Cobertura baixa

**Solução**:
```bash
# Ver quais linhas não estão cobertas
mvn jacoco:report
firefox target/site/jacoco/index.html

# Adicionar testes para linhas em vermelho
```

---

## 📚 Versionamento Semântico

Siga o padrão: `REL-MAJOR.MINOR.PATCH`

- **MAJOR** (1.0.0 → 2.0.0): Breaking changes
- **MINOR** (1.0.0 → 1.1.0): Novas funcionalidades (compatível)
- **PATCH** (1.0.0 → 1.0.1): Correções de bugs

**Exemplos:**
```
REL-0.0.1  → Primeiro alpha
REL-0.1.0  → Beta com novas features
REL-1.0.0  → Primeira versão de produção
REL-1.0.1  → Correção de bug
REL-1.1.0  → Nova funcionalidade
REL-2.0.0  → Breaking change
```

---

## 🎉 Resumo

**Para fazer uma release:**

```bash
# 1. Criar tag
git tag -a REL-1.0.0 -m "Release 1.0.0"

# 2. Push (DISPARA AUTOMAÇÃO!)
git push origin REL-1.0.0

# 3. Aguardar 3-5 minutos

# 4. Release pronta! 🎉
```

**A automação cuida de tudo:**
- ✅ Testes
- ✅ Cobertura
- ✅ Empacotamento
- ✅ Documentação
- ✅ Publicação

---

**Última atualização**: 28 de Outubro de 2024
