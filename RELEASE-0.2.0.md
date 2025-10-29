# 🐳 Release Notes - v0.2.0

**Data**: 28 de Outubro de 2024  
**Tema**: Docker Container & Multi-Database Support  
**Status**: Pronto para Release

---

## 🎯 Objetivo da Release

Implementar containerização completa do aiToSql MCP Server, permitindo deploy fácil via Docker em qualquer ambiente com suporte a múltiplos bancos de dados relacionais.

---

## ✨ Novidades

### 🐳 Docker Container

- **Dockerfile Multi-Stage Otimizado**
  - Build stage com Maven para compilação
  - Runtime stage com JRE 17 Alpine (imagem leve)
  - Imagem final: ~180MB (otimizado!)
  - Health check integrado
  - Non-root user para segurança

### ⚙️ Configuração via Variáveis de Ambiente

Todas as configurações agora podem ser feitas via ENV:

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `DB_URL` | URL JDBC de conexão | `jdbc:postgresql://host:5432/db` |
| `DB_USERNAME` | Usuário do banco (READ-ONLY) | `readonly_user` |
| `DB_PASSWORD` | Senha do banco | `secure_password` |
| `DB_DRIVER` | Classe do driver JDBC | `org.postgresql.Driver` |
| `DB_TYPE` | Tipo do banco | `PostgreSQL`, `MySQL`, `SQLServer` |
| `SERVER_PORT` | Porta do servidor | `8080` (default) |
| `CACHE_ENABLED` | Habilitar cache | `true` (default) |
| `LOGGING_LEVEL_ROOT` | Nível de log global | `INFO` (default) |

### 🗄️ Suporte Multi-Database

Suporte nativo e testado para:

1. **PostgreSQL** 
   - Driver incluído: `org.postgresql.Driver`
   - Docker Compose: `docker-compose-postgres.yml`
   - Init script: `docker/postgres/init.sql`

2. **MySQL**
   - Driver incluído: `com.mysql.cj.jdbc.Driver`
   - Docker Compose: `docker-compose-mysql.yml`
   - Init script: `docker/mysql/init.sql`

3. **SQL Server**
   - Driver incluído: `com.microsoft.sqlserver.jdbc.SQLServerDriver`
   - Docker Compose: `docker-compose-sqlserver.yml`
   - Init script: `docker/sqlserver/init.sql`

### 🚀 Docker Compose Files

3 arquivos Docker Compose prontos para desenvolvimento local:

```bash
# PostgreSQL
docker-compose -f docker-compose-postgres.yml up -d

# MySQL
docker-compose -f docker-compose-mysql.yml up -d

# SQL Server
docker-compose -f docker-compose-sqlserver.yml up -d
```

Cada compose file inclui:
- Banco de dados configurado
- MCP Server conectado
- Schema de exemplo (customers, products, orders)
- Usuário read-only criado
- Health checks
- Volumes para persistência

### 🤖 GitHub Actions CI/CD

Workflow automático para Docker Hub:

- **Trigger 1**: Push para branch `main` → tag `main`
- **Trigger 2**: Push de tag `REL-X.Y.Z` → tags `X.Y.Z`, `X.Y`, `X`, `latest`
- **Multi-arch**: Build para `linux/amd64` e `linux/arm64`
- **Auto-deploy**: Publica automaticamente no Docker Hub

### 📜 Scripts Automatizados

1. **docker-build-and-push.sh**
   - Build local da imagem
   - Teste automático
   - Push para Docker Hub (com confirmação)
   - Criação de tags semânticas

2. **test-docker-deployment.sh**
   - Testa build da imagem
   - Testa PostgreSQL compose
   - Testa MySQL compose
   - Verifica health checks
   - Testa endpoints MCP

### 📚 Documentação

Novos documentos criados:

1. **DOCKER_README.md**
   - Descrição para Docker Hub
   - Quick start
   - Exemplos de uso
   - Tabela de variáveis

2. **DOCKER_DEPLOYMENT.md**
   - Guia completo de deployment
   - Configuração por banco
   - Segurança e best practices
   - Troubleshooting

3. **DOCKER_BUILD_GUIDE.md**
   - Como buildar localmente
   - Como publicar no Docker Hub
   - GitHub Actions setup
   - Multi-arch build

---

## 🔧 Mudanças Técnicas

### application.properties

Atualizado para suportar ENV vars:

```properties
# Antes (fixo)
server.port=8080
spring.datasource.username=mcp_readonly_user

# Agora (configurável)
server.port=${SERVER_PORT:8080}
spring.datasource.username=${DB_USERNAME:mcp_readonly_user}
```

### application-docker.properties

Novo profile específico para Docker:

- Logging otimizado para containers
- Pool de conexões ajustado
- Actuator endpoints expostos
- Health checks habilitados

### pom.xml

Drivers incluídos:

```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
</dependency>
```

---

## 📊 Métricas

### Antes (v0.1.0)
- ❌ Sem Docker
- ❌ Configuração manual
- ❌ Deploy complexo
- ✅ Cobertura: 74%

### Agora (v0.2.0)
- ✅ Docker image: ~180MB
- ✅ Configuração 100% via ENV
- ✅ Deploy com 1 comando
- ✅ CI/CD automático
- ✅ Multi-database
- ✅ Cobertura: 74% (mantida)

### Performance

- **Build time**: ~3 minutos (local)
- **Startup time**: ~15 segundos (com H2)
- **Startup time**: ~30 segundos (com PostgreSQL)
- **Memory usage**: ~250MB (runtime)
- **Image size**: ~180MB

---

## 🚀 Como Usar

### Opção 1: Docker Run (Mais Simples)

```bash
docker pull flaviomagacho/aitosql:0.2.0

docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:postgresql://your-host:5432/your_db" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -e DB_TYPE="PostgreSQL" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -p 8080:8080 \
  flaviomagacho/aitosql:0.2.0
```

### Opção 2: Docker Compose (Recomendado)

```bash
# Clone o repositório
git clone https://github.com/magacho/aiToSql.git
cd aiToSql

# Escolha seu banco e inicie
docker-compose -f docker-compose-postgres.yml up -d

# Verifique os logs
docker-compose -f docker-compose-postgres.yml logs -f mcp-server

# Teste
curl http://localhost:8080/actuator/health
curl http://localhost:8080/mcp/tools/list
```

---

## 🔒 Segurança

**IMPORTANTE**: Sempre use usuário READ-ONLY!

### PostgreSQL

```sql
CREATE USER readonly_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE your_db TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
```

### MySQL

```sql
CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT ON your_db.* TO 'readonly_user'@'%';
FLUSH PRIVILEGES;
```

### SQL Server

```sql
CREATE LOGIN readonly_user WITH PASSWORD = 'SecurePassword123!';
USE your_database;
CREATE USER readonly_user FOR LOGIN readonly_user;
GRANT SELECT TO readonly_user;
```

---

## 🐛 Problemas Conhecidos

Nenhum problema crítico identificado nesta release.

### Limitações

1. **Oracle Driver**: Não incluído por padrão (requer licença)
   - Solução: Adicionar manualmente se necessário
   
2. **Secrets Management**: Passwords via ENV vars
   - Melhoria planejada: Docker Secrets (v0.3.0)

---

## 🔄 Breaking Changes

**Nenhuma breaking change**. Totalmente compatível com v0.1.0.

---

## 📝 Checklist de Release

- [x] Código implementado
- [x] Dockerfile criado e testado
- [x] Docker Compose files criados
- [x] Scripts de inicialização (init.sql)
- [x] GitHub Actions configurado
- [x] Documentação completa
- [x] Scripts automatizados
- [x] Testes locais (Docker)
- [ ] **Publicação no Docker Hub** (aguardando usuário)
- [ ] **Tag git criada** (REL-0.2.0)
- [ ] **GitHub release publicado**

---

## 🎯 Próximos Passos (v0.3.0)

Planejado para Dezembro 2024:

1. **Integração com LLMs Reais**
   - OpenAI GPT-4
   - Anthropic Claude
   - Google Gemini
   - Ollama (local)

2. **Text-to-SQL Intelligence**
   - `naturalLanguageQuery` tool
   - `explainQuery` tool
   - Query optimization suggestions

3. **Cost Tracking Dashboard**
   - Analytics endpoint
   - Web dashboard simples
   - Usage reports

---

## 🤝 Como Contribuir

```bash
# Fork e clone
git clone https://github.com/SEU_USUARIO/aiToSql.git

# Criar branch
git checkout -b feature/minha-feature

# Testar Docker
docker-compose -f docker-compose-postgres.yml up -d
./test-docker-deployment.sh

# Commit e PR
git commit -m "feat: adicionar nova feature"
git push origin feature/minha-feature
```

---

## 🔗 Links

- **Docker Hub**: https://hub.docker.com/r/flaviomagacho/aitosql (aguardando publicação)
- **GitHub**: https://github.com/magacho/aiToSql
- **Issues**: https://github.com/magacho/aiToSql/issues
- **Discussions**: https://github.com/magacho/aiToSql/discussions

---

## 👏 Agradecimentos

Obrigado por usar o aiToSql MCP Server! Feedback e contribuições são muito bem-vindos.

---

**Versão**: 0.2.0  
**Build**: 28/Out/2024  
**Mantenedor**: @magacho  
**Licença**: MIT
