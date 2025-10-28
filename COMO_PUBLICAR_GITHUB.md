# Guia Completo: Comitar e Publicar no GitHub

## 🎯 Passo a Passo Completo

### Passo 1: Inicializar Git Local

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql

# Executar script de inicialização
./INICIALIZAR_GIT.sh
```

O script irá:
- ✅ Inicializar repositório Git
- ✅ Configurar nome e email (se necessário)
- ✅ Adicionar todos os arquivos
- ✅ Criar commit inicial completo
- ✅ Mostrar próximos passos

---

### Passo 2: Criar Repositório no GitHub

#### Opção A: Via Interface Web (Mais Fácil)

1. **Acesse**: https://github.com/new

2. **Preencha os campos**:
   - **Repository name**: `aiToSql` ou `PromptToSql`
   - **Description**: `MCP Server - Database Introspection Tool for LLMs (JSON-RPC 2.0)`
   - **Visibility**: 
     - ✅ Public (recomendado - para badges e CI gratuito)
     - ⬜ Private
   - **NÃO marque**:
     - ⬜ Add a README file
     - ⬜ Add .gitignore
     - ⬜ Choose a license

3. **Clique**: "Create repository"

4. **Copie a URL** que aparecerá (exemplo):
   ```
   https://github.com/seu-usuario/aiToSql.git
   ```

#### Opção B: Via CLI (gh)

```bash
# Criar repositório público
gh repo create aiToSql --public --description "MCP Server - Database Introspection Tool"

# OU criar privado
gh repo create aiToSql --private --description "MCP Server - Database Introspection Tool"
```

---

### Passo 3: Conectar Local com GitHub

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql

# Adicionar remote (substitua SEU_USUARIO)
git remote add origin https://github.com/SEU_USUARIO/aiToSql.git

# Verificar
git remote -v
```

**Você verá:**
```
origin  https://github.com/SEU_USUARIO/aiToSql.git (fetch)
origin  https://github.com/SEU_USUARIO/aiToSql.git (push)
```

---

### Passo 4: Renomear Branch para Main

```bash
# Renomear branch atual para main
git branch -M main

# Verificar
git branch
```

---

### Passo 5: Fazer Push Inicial

```bash
# Push para GitHub
git push -u origin main
```

**Autenticação:**

Se pedir senha, use **Personal Access Token** (não a senha do GitHub):

1. Gere token em: https://github.com/settings/tokens
2. Permissões necessárias: `repo`, `workflow`
3. Use o token como senha

Ou configure SSH:
```bash
# Gerar chave SSH (se não tiver)
ssh-keygen -t ed25519 -C "seu-email@example.com"

# Adicionar no GitHub
cat ~/.ssh/id_ed25519.pub
# Copie e cole em: https://github.com/settings/keys

# Mudar remote para SSH
git remote set-url origin git@github.com:SEU_USUARIO/aiToSql.git
```

---

### Passo 6: Verificar no GitHub

Acesse: `https://github.com/SEU_USUARIO/aiToSql`

Você deve ver:
- ✅ 16 classes Java
- ✅ 8 documentos .md
- ✅ Scripts de automação
- ✅ GitHub Actions workflows
- ✅ Testes completos

---

### Passo 7: Ativar GitHub Actions

1. Vá para: `https://github.com/SEU_USUARIO/aiToSql/actions`
2. Clique em "I understand my workflows, go ahead and enable them"

Agora os workflows estão ativos!

---

### Passo 8: Criar Primeira Release (Opcional)

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql

# Criar tag de release
git tag -a REL-0.0.1 -m "Release 0.0.1 - Versão inicial

- Protocolo JSON-RPC 2.0 completo
- 4 ferramentas MCP implementadas
- 25 testes automatizados (cobertura 82%)
- Suporte multi-banco
- Documentação completa"

# Push da tag (DISPARA AUTOMAÇÃO!)
git push origin REL-0.0.1
```

**Aguarde 3-5 minutos** e veja a release em:
`https://github.com/SEU_USUARIO/aiToSql/releases`

---

## 📋 Resumo dos Comandos

```bash
# 1. Inicializar Git
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
./INICIALIZAR_GIT.sh

# 2. Adicionar remote (substitua SEU_USUARIO!)
git remote add origin https://github.com/SEU_USUARIO/aiToSql.git

# 3. Renomear branch
git branch -M main

# 4. Push inicial
git push -u origin main

# 5. (Opcional) Criar release
git tag -a REL-0.0.1 -m "Release 0.0.1 - Versão inicial"
git push origin REL-0.0.1
```

---

## 🔍 Verificação Pós-Upload

### Verificar Estrutura

```bash
# Ver arquivos no repositório
gh repo view --web

# Ou via browser
# https://github.com/SEU_USUARIO/aiToSql
```

**Você deve ver:**

```
aiToSql/
├── .github/
│   └── workflows/
│       ├── ci.yml              ✅
│       └── release.yml         ✅
├── src/
│   ├── main/
│   │   ├── java/              ✅ (16 classes)
│   │   └── resources/         ✅
│   └── test/
│       ├── java/              ✅ (3 classes de teste)
│       └── resources/         ✅
├── README.md                   ✅
├── QUICKSTART.md              ✅
├── TESTING_GUIDE.md           ✅
├── COVERAGE_REPORT.md         ✅
├── RELEASE_HISTORY.md         ✅
├── pom.xml                    ✅
├── .gitignore                 ✅
└── (outros 10+ arquivos)      ✅
```

### Verificar GitHub Actions

```bash
# Via CLI
gh run list

# Via browser
# https://github.com/SEU_USUARIO/aiToSql/actions
```

**Após primeiro push, você verá:**
- ✅ CI workflow rodando
- ✅ Testes sendo executados
- ✅ Relatório de cobertura

---

## 🎨 Adicionar Badges ao README (Opcional)

Edite o `README.md` e adicione no topo:

```markdown
# MCP Server - Database Introspection Tool

[![CI Tests](https://github.com/SEU_USUARIO/aiToSql/workflows/CI%20-%20Testes%20Automáticos/badge.svg)](https://github.com/SEU_USUARIO/aiToSql/actions)
[![Coverage](https://img.shields.io/badge/coverage-82%25-green)](./COVERAGE_REPORT.md)
[![Tests](https://img.shields.io/badge/tests-25%20passing-brightgreen)](./TESTING_GUIDE.md)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.2.1-green.svg)](https://spring.io/projects/spring-boot)
```

Depois commit e push:

```bash
git add README.md
git commit -m "docs: adicionar badges ao README"
git push
```

---

## 🔐 Configurar Secrets (Para CI/CD)

Se quiser integrar com serviços externos:

1. Vá para: `https://github.com/SEU_USUARIO/aiToSql/settings/secrets/actions`

2. Adicione secrets necessários:
   - `CODECOV_TOKEN` (se usar Codecov)
   - `SONAR_TOKEN` (se usar SonarCloud)

---

## 🌟 Tornar Repositório Público (Se Privado)

```bash
# Via CLI
gh repo edit --visibility public

# Ou via web
# Settings → Danger Zone → Change repository visibility
```

---

## 📊 Monitorar Primeira Execução do CI

```bash
# Acompanhar em tempo real
gh run watch

# Ver logs
gh run view --log

# Ver status
gh run list
```

---

## 🎯 Checklist Final

Antes de considerar completo, verifique:

- [ ] ✅ Repositório criado no GitHub
- [ ] ✅ Remote origin configurado
- [ ] ✅ Push inicial feito com sucesso
- [ ] ✅ GitHub Actions ativado
- [ ] ✅ CI workflow rodou e passou
- [ ] ✅ Arquivos visíveis no GitHub
- [ ] ✅ README.md formatado corretamente
- [ ] ✅ Documentação acessível
- [ ] ✅ (Opcional) Release criada

---

## 🚨 Troubleshooting

### Erro: "Permission denied"

**Solução**: Configure SSH ou use Personal Access Token

```bash
# Gerar token
# https://github.com/settings/tokens/new
# Permissões: repo, workflow

# Usar como senha ao fazer push
```

### Erro: "Repository not found"

**Solução**: Verifique se o repositório foi criado e a URL está correta

```bash
# Ver remote atual
git remote -v

# Corrigir se necessário
git remote set-url origin https://github.com/SEU_USUARIO/aiToSql.git
```

### Erro: "Updates were rejected"

**Solução**: Pull antes de push (se houver algo no GitHub)

```bash
git pull origin main --rebase
git push origin main
```

---

## 📚 Próximos Passos Após Upload

1. **Convidar colaboradores** (se houver):
   - Settings → Collaborators → Add people

2. **Configurar branch protection**:
   - Settings → Branches → Add rule
   - Exigir CI passar antes de merge

3. **Adicionar LICENSE**:
   ```bash
   # Via web: Add file → Create new file → LICENSE
   # Escolha MIT, Apache 2.0, etc.
   ```

4. **Configurar Issues/Projects**:
   - Settings → Features → Issues (ativar)

5. **Compartilhar**:
   ```
   🎉 MCP Server publicado!
   https://github.com/SEU_USUARIO/aiToSql
   ```

---

## 🎉 Pronto!

Seu projeto está no GitHub com:
- ✅ 16 classes Java
- ✅ 25 testes automatizados
- ✅ CI/CD completo
- ✅ Automação de releases
- ✅ Documentação completa

**Execute os comandos acima e seu projeto estará online!** 🚀
