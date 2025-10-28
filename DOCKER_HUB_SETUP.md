# 🐳 Guia de Publicação no Docker Hub

Este guia explica como configurar o GitHub Actions para publicar automaticamente as imagens Docker no Docker Hub.

## 📋 Pré-requisitos

1. **Conta no Docker Hub**: https://hub.docker.com
2. **Repositório Criado**: `magacho/aitosql-mcp-server`
3. **Access Token do Docker Hub**: Necessário para autenticação segura

---

## 🔐 Criar Access Token no Docker Hub

### Passo 1: Login no Docker Hub
1. Acesse https://hub.docker.com
2. Faça login com suas credenciais

### Passo 2: Gerar Access Token
1. Clique no seu nome de usuário (canto superior direito)
2. Selecione **Account Settings**
3. Vá para **Security** → **Access Tokens**
4. Clique em **New Access Token**
5. Configure:
   - **Description**: `GitHub Actions - aiToSql`
   - **Access permissions**: `Read, Write, Delete`
6. Clique em **Generate**
7. **IMPORTANTE**: Copie o token imediatamente (não será mostrado novamente!)

---

## ⚙️ Configurar Secrets no GitHub

### Passo 1: Acessar Configurações do Repositório
1. Vá para o repositório: https://github.com/magacho/aiToSql
2. Clique em **Settings** (configurações)
3. No menu lateral, clique em **Secrets and variables** → **Actions**

### Passo 2: Adicionar Secrets
Clique em **New repository secret** e adicione:

#### Secret 1: DOCKERHUB_USERNAME
- **Name**: `DOCKERHUB_USERNAME`
- **Value**: `magacho` (seu usuário do Docker Hub)
- Clique em **Add secret**

#### Secret 2: DOCKERHUB_TOKEN
- **Name**: `DOCKERHUB_TOKEN`
- **Value**: Cole o Access Token que você copiou do Docker Hub
- Clique em **Add secret**

---

## 🚀 Como Funciona

### Quando as Imagens são Publicadas?

#### 1. Em cada Commit (CI)
```yaml
# Workflow: .github/workflows/ci.yml
on:
  push:
    branches: [ '**' ]
```
- Executa testes
- Faz build da imagem Docker (sem publicar)
- Valida que a imagem pode ser construída

#### 2. Em Releases (Tags REL-*)
```yaml
# Workflow: .github/workflows/release.yml
on:
  push:
    tags:
      - 'REL-*'
```
- Executa todos os testes
- Gera relatório de cobertura
- Faz build da imagem Docker
- **Publica no Docker Hub** com múltiplas tags

---

## 📦 Tags Publicadas

Quando você cria uma release `REL-0.3.0`, as seguintes tags são publicadas:

```bash
magacho/aitosql-mcp-server:latest
magacho/aitosql-mcp-server:0.3.0
magacho/aitosql-mcp-server:v0.3.0
```

---

## 🎯 Criar uma Release (Exemplo)

### Método 1: Via Linha de Comando

```bash
# 1. Atualizar versão no pom.xml
mvn versions:set -DnewVersion=0.3.0

# 2. Commit
git add pom.xml
git commit -m "chore: preparar release 0.3.0"

# 3. Criar tag
git tag REL-0.3.0

# 4. Push (dispara CI/CD)
git push origin main
git push origin REL-0.3.0
```

### Método 2: Via GitHub CLI

```bash
# Criar tag e release em um comando
gh release create REL-0.3.0 \
  --title "Release v0.3.0" \
  --notes "Nova versão com melhorias"
```

---

## 🔍 Verificar Publicação

### 1. No GitHub Actions
1. Vá para: https://github.com/magacho/aiToSql/actions
2. Encontre o workflow **"Release Automático"**
3. Verifique se o job **"Publicar Imagem Docker"** foi bem-sucedido

### 2. No Docker Hub
1. Acesse: https://hub.docker.com/r/magacho/aitosql-mcp-server
2. Vá para **Tags**
3. Verifique se as tags foram criadas

### 3. Testar Download
```bash
# Pull da imagem
docker pull magacho/aitosql-mcp-server:0.3.0

# Verificar imagem
docker images | grep aitosql

# Testar execução
docker run --rm magacho/aitosql-mcp-server:0.3.0 --version
```

---

## 🏗️ Arquiteturas Suportadas

As imagens são construídas para múltiplas arquiteturas:

- **linux/amd64** (x86_64) - Servidores Intel/AMD
- **linux/arm64** (aarch64) - Apple Silicon, Raspberry Pi 4+

```bash
# Build multi-arquitetura (feito automaticamente pelo CI/CD)
docker buildx build --platform linux/amd64,linux/arm64 .
```

---

## 🔧 Troubleshooting

### Erro: "unauthorized: authentication required"
- **Causa**: Token do Docker Hub inválido ou expirado
- **Solução**: Gerar novo token e atualizar secret `DOCKERHUB_TOKEN`

### Erro: "requested access to the resource is denied"
- **Causa**: Usuário não tem permissão no repositório
- **Solução**: Verificar se o repositório `magacho/aitosql-mcp-server` existe e você tem acesso

### Imagem não aparece no Docker Hub
- **Causa**: Workflow pode ter falhado
- **Solução**: Verificar logs em GitHub Actions → Release Automático

---

## 📚 Recursos Adicionais

- **Docker Hub Docs**: https://docs.docker.com/docker-hub/
- **GitHub Actions Docs**: https://docs.github.com/en/actions
- **Docker Buildx**: https://docs.docker.com/buildx/working-with-buildx/

---

## 🎉 Pronto!

Agora seu projeto está configurado para publicação automática no Docker Hub a cada release! 🚀

**Próximos Passos**:
1. Criar primeira release: `git tag REL-0.3.0 && git push origin REL-0.3.0`
2. Verificar publicação no Docker Hub
3. Testar imagem publicada
4. Compartilhar com a comunidade! 🌟
