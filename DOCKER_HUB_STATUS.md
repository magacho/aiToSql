# Status da Publicação no Docker Hub

## ❌ Status Atual: NÃO PUBLICADO

As imagens Docker **ainda NÃO estão sendo publicadas** no Docker Hub.

## 🔍 Diagnóstico

### Workflow Configurado ✅
O workflow `.github/workflows/release.yml` possui um job `docker-publish` que:
- Depende do job `release` completar com sucesso
- Faz build multi-arquitetura (amd64, arm64)
- Publica 3 tags:
  - `magacho/aitosql-mcp-server:latest`
  - `magacho/aitosql-mcp-server:X.X.X`
  - `magacho/aitosql-mcp-server:vX.X.X`

### Problema Identificado ❌
O job `release` está falhando com **erro 403** ao tentar criar a release no GitHub:
```
⚠️ GitHub release failed with status: 403
undefined
```

### Causa Provável
- Permissões insuficientes do `GITHUB_TOKEN`
- Job não herda permissões corretamente

### Correção Aplicada ✅
**Commit**: `336bd34` - Adicionadas permissões explícitas nos jobs:
```yaml
permissions:
  contents: write
  packages: write
  id-token: write

jobs:
  release:
    permissions:
      contents: write
      packages: write
      
  docker-publish:
    permissions:
      contents: read
      packages: write
```

## 🔐 Secrets Necessárias

Para publicar no Docker Hub, é necessário configurar as seguintes secrets no GitHub:

### Verificar/Criar Secrets

```bash
# Listar secrets existentes
gh secret list

# Criar secrets (se necessário)
gh secret set DOCKERHUB_USERNAME --body "magacho"
gh secret set DOCKERHUB_TOKEN --body "seu-token-do-dockerhub"
```

### Como Obter o Token do Docker Hub

1. Acesse https://hub.docker.com/settings/security
2. Clique em "New Access Token"
3. Defina um nome (ex: "github-actions-aitosql")
4. Permissões: Read, Write, Delete
5. Copie o token gerado
6. Configure no GitHub usando o comando acima

## ✅ Próximos Passos

1. **Verificar se as secrets existem**:
   ```bash
   gh secret list
   ```

2. **Se não existirem, criar as secrets**:
   ```bash
   gh secret set DOCKERHUB_USERNAME --body "magacho"
   gh secret set DOCKERHUB_TOKEN --body "SEU_TOKEN_AQUI"
   ```

3. **Testar o workflow com uma nova tag**:
   ```bash
   # Criar uma tag de teste
   git tag REL-0.2.1
   git push origin REL-0.2.1
   ```

4. **Verificar os logs do workflow**:
   - Acesse: https://github.com/magacho/aiToSql/actions
   - Verifique se o job `release` completa com sucesso
   - Verifique se o job `docker-publish` executa e publica

## 📦 Releases Anteriores (Falhadas)

| Tag | Data | Status | Motivo |
|-----|------|--------|---------|
| REL-0.2.0 | 28/10/2025 | ❌ Falhou | Erro 403 - Permissões |
| REL-0.1.0 | 28/10/2025 | ❌ Falhou | Erro 403 - Permissões |

## 🎯 Resultado Esperado

Após a correção e configuração das secrets, ao criar uma tag `REL-X.X.X`:

1. ✅ Testes executam
2. ✅ Relatório de cobertura gerado
3. ✅ Release criada no GitHub com artefatos
4. ✅ Imagem Docker construída
5. ✅ Imagem publicada no Docker Hub
6. ✅ README.md atualizado no Docker Hub

## 🔗 Links Úteis

- **Docker Hub Repository**: https://hub.docker.com/r/magacho/aitosql-mcp-server (ainda não existe)
- **GitHub Actions**: https://github.com/magacho/aiToSql/actions
- **Workflow Release**: https://github.com/magacho/aiToSql/blob/main/.github/workflows/release.yml

---

**Última Atualização**: 29/10/2025 18:38 UTC
**Status**: Aguardando configuração de secrets e teste de nova release
