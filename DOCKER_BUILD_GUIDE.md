# 🐳 Guia Completo de Build e Publicação Docker

Este guia explica como construir e publicar a imagem Docker do **aiToSql MCP Server** no Docker Hub.

---

## 📋 Pré-requisitos

1. **Docker instalado e rodando**
   ```bash
   docker --version
   # Deve mostrar: Docker version XX.X.X
   ```

2. **Conta no Docker Hub**
   - Criar conta em: https://hub.docker.com
   - Username: `magacho` (ou seu username)

3. **Login no Docker Hub**
   ```bash
   docker login
   # Username: magacho
   # Password: [sua senha ou token]
   ```

---

## 🏗️ Build Local da Imagem

### Opção 1: Build Manual

```bash
# Navegar para o diretório do projeto
cd /caminho/para/PromptToSql

# Build da imagem
docker build -t magacho/aitosql-mcp-server:0.2.0 .
docker build -t magacho/aitosql-mcp-server:latest .

# Verificar imagem criada
docker images | grep aitosql
```

### Opção 2: Usar o Script Automatizado

```bash
# Build e publicar versão específica
./docker-build-and-push.sh 0.2.0

# Build apenas (sem publicar)
./docker-build-and-push.sh 0.2.0
# Quando perguntado sobre publicar, responder 'n'
```

---

## 🧪 Testar a Imagem Localmente

### Teste Rápido com H2 (sem banco externo)

```bash
docker run --rm -d \
  --name test-mcp \
  -e DB_URL="jdbc:h2:mem:testdb" \
  -e DB_USERNAME="sa" \
  -e DB_PASSWORD="" \
  -e DB_TYPE="H2" \
  -e DB_DRIVER="org.h2.Driver" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest

# Aguardar 15 segundos
sleep 15

# Testar health check
curl http://localhost:8080/actuator/health

# Listar ferramentas MCP
curl http://localhost:8080/mcp/tools/list

# Parar container
docker stop test-mcp
```

### Teste com PostgreSQL (Docker Compose)

```bash
# Iniciar PostgreSQL + MCP Server
docker-compose -f docker-compose-postgres.yml up -d

# Ver logs
docker-compose -f docker-compose-postgres.yml logs -f mcp-server

# Aguardar 30 segundos
sleep 30

# Testar
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"getSchemaStructure"}'

# Parar
docker-compose -f docker-compose-postgres.yml down -v
```

### Teste com MySQL (Docker Compose)

```bash
# Iniciar MySQL + MCP Server
docker-compose -f docker-compose-mysql.yml up -d

# Ver logs
docker-compose -f docker-compose-mysql.yml logs -f mcp-server

# Testar (após 30 segundos)
curl http://localhost:8080/actuator/health

# Parar
docker-compose -f docker-compose-mysql.yml down -v
```

### Teste Completo Automatizado

```bash
# Roda todos os testes (PostgreSQL + MySQL)
./test-docker-deployment.sh
```

---

## 📤 Publicar no Docker Hub

### Método 1: Usando o Script

```bash
./docker-build-and-push.sh 0.2.0
# Responder 'y' quando perguntado sobre publicar
```

O script automaticamente:
- ✅ Faz build da imagem
- ✅ Cria tags: `0.2.0`, `0.2`, `0`, `latest`
- ✅ Testa a imagem localmente
- ✅ Faz push para Docker Hub

### Método 2: Manual

```bash
# 1. Build
docker build -t magacho/aitosql-mcp-server:0.2.0 .

# 2. Criar tags
docker tag magacho/aitosql-mcp-server:0.2.0 magacho/aitosql-mcp-server:0.2
docker tag magacho/aitosql-mcp-server:0.2.0 magacho/aitosql-mcp-server:0
docker tag magacho/aitosql-mcp-server:0.2.0 magacho/aitosql-mcp-server:latest

# 3. Login (se ainda não estiver logado)
docker login

# 4. Push
docker push magacho/aitosql-mcp-server:0.2.0
docker push magacho/aitosql-mcp-server:0.2
docker push magacho/aitosql-mcp-server:0
docker push magacho/aitosql-mcp-server:latest
```

---

## 🔄 Publicação Automática via GitHub Actions

O projeto já tem GitHub Actions configurado para publicar automaticamente:

### Triggers

1. **Push para branch main**
   - Tag: `main`

2. **Push de tag com prefixo REL-**
   - Exemplo: `REL-0.2.0` → tags: `0.2.0`, `0.2`, `0`, `latest`

### Configurar Secrets no GitHub

1. Ir em: **Settings** → **Secrets and variables** → **Actions**

2. Adicionar secrets:
   - `DOCKERHUB_USERNAME`: `magacho`
   - `DOCKERHUB_TOKEN`: [seu token do Docker Hub]

### Como obter Docker Hub Token

1. Ir em: https://hub.docker.com/settings/security
2. Clicar em **New Access Token**
3. Name: `github-actions`
4. Permissions: **Read, Write, Delete**
5. Copiar o token (só mostra uma vez!)

### Publicar via GitHub Actions

```bash
# Opção 1: Push com tag de release
git tag REL-0.2.0
git push origin REL-0.2.0

# Opção 2: Push para branch main
git push origin main

# Ver progresso
# GitHub → Actions → Docker Build and Push
```

---

## 🎯 Verificar Publicação

### Docker Hub

1. Acessar: https://hub.docker.com/r/magacho/aitosql-mcp-server
2. Verificar:
   - ✅ Tags: `0.2.0`, `0.2`, `0`, `latest`
   - ✅ Descrição (do DOCKER_README.md)
   - ✅ Tamanho da imagem (deve ser ~200MB)

### Testar Pull da Imagem Pública

```bash
# Pull
docker pull magacho/aitosql-mcp-server:latest

# Rodar
docker run -d \
  --name test-public \
  -e DB_URL="jdbc:h2:mem:testdb" \
  -e DB_USERNAME="sa" \
  -e DB_PASSWORD="" \
  -e DB_TYPE="H2" \
  -e DB_DRIVER="org.h2.Driver" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest

# Testar
curl http://localhost:8080/actuator/health

# Limpar
docker stop test-public
docker rm test-public
```

---

## 📊 Multi-Architecture Build (Opcional)

Para suportar múltiplas arquiteturas (amd64, arm64):

```bash
# 1. Criar builder
docker buildx create --name multiarch --use
docker buildx inspect --bootstrap

# 2. Build multi-arch
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t magacho/aitosql-mcp-server:0.2.0 \
  -t magacho/aitosql-mcp-server:latest \
  --push \
  .
```

**Nota**: GitHub Actions já faz build multi-arch automaticamente.

---

## 🐛 Troubleshooting

### Problema: "denied: requested access to the resource is denied"

**Solução**: Fazer login no Docker Hub
```bash
docker login
```

### Problema: "Cannot connect to the Docker daemon"

**Solução**: Iniciar o Docker Desktop ou Docker daemon
```bash
# Linux
sudo systemctl start docker

# macOS/Windows
# Abrir Docker Desktop
```

### Problema: Build muito lento

**Solução**: Usar cache do Docker
```bash
docker build --cache-from magacho/aitosql-mcp-server:latest \
  -t magacho/aitosql-mcp-server:0.2.0 .
```

### Problema: Imagem muito grande

**Solução**: Verificar .dockerignore
```bash
# Ver arquivos sendo copiados
docker build --no-cache --progress=plain -t test . 2>&1 | grep "COPY"

# Tamanho atual
docker images magacho/aitosql-mcp-server:latest
# Deve ser ~200MB
```

---

## 📝 Checklist de Publicação

Antes de publicar uma nova versão:

- [ ] Código testado localmente (`mvn clean test`)
- [ ] Cobertura de testes atualizada
- [ ] README.md atualizado com nova versão
- [ ] ROADMAP.md atualizado
- [ ] Build Docker local funcionando
- [ ] Testes Docker funcionando (`./test-docker-deployment.sh`)
- [ ] Changelog/Release notes criado
- [ ] Tag git criada (`git tag REL-X.Y.Z`)
- [ ] Push para GitHub (`git push origin REL-X.Y.Z`)
- [ ] GitHub Actions rodou com sucesso
- [ ] Imagem disponível no Docker Hub
- [ ] Pull e teste da imagem pública

---

## 🔗 Links Úteis

- **Docker Hub**: https://hub.docker.com/r/magacho/aitosql-mcp-server
- **GitHub Actions**: https://github.com/magacho/aiToSql/actions
- **Dockerfile**: `/Dockerfile`
- **Docker Compose**: `/docker-compose-*.yml`

---

## 🎓 Comandos Docker Úteis

```bash
# Listar imagens locais
docker images | grep aitosql

# Remover imagem local
docker rmi magacho/aitosql-mcp-server:0.2.0

# Ver logs de container
docker logs -f container-name

# Inspecionar container
docker inspect container-name

# Ver recursos usados
docker stats container-name

# Limpar tudo (cuidado!)
docker system prune -a

# Ver tamanho de layers
docker history magacho/aitosql-mcp-server:latest
```

---

**Dúvidas?** Abrir issue em: https://github.com/magacho/aiToSql/issues
