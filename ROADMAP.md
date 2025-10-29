# 🗺️ Roadmap - aiToSql MCP Server

**Data de Criação**: 28 de Outubro de 2024  
**Última Atualização**: 28 de Outubro de 2024  
**Status Atual**: v0.2.0 (Containerização Completa)  
**Releases Publicadas**: v0.1.0, v0.2.0 (em preparação para release)

---

## 🎯 Visão Geral

Este roadmap define a evolução do **aiToSql MCP Server** de um MVP funcional para uma solução enterprise-grade de integração entre LLMs e bancos de dados relacionais.

---

## 📋 Histórico de Releases

### ✅ v0.2.0 - COMPLETO (28/Out/2024)

**Tema**: Docker Container & Multi-Database Support

**Entregas Principais:**
- ✅ Dockerfile multi-stage otimizado (< 200MB)
- ✅ Configuração 100% via variáveis de ambiente
- ✅ Suporte completo a PostgreSQL, MySQL e SQL Server
- ✅ 3 Docker Compose files (um para cada banco)
- ✅ Scripts de inicialização de banco (init.sql)
- ✅ GitHub Actions para build e push automático
- ✅ Multi-architecture support (amd64, arm64)
- ✅ application-docker.properties para profile Docker
- ✅ Scripts automatizados:
  - docker-build-and-push.sh
  - test-docker-deployment.sh
- ✅ Documentação completa:
  - DOCKER_README.md (para Docker Hub)
  - DOCKER_DEPLOYMENT.md (guia de uso)
  - DOCKER_BUILD_GUIDE.md (guia de build)

**Métricas Alcançadas:**
- ⏱️ Tempo de build: ~3 minutos
- 📦 Tamanho da imagem: ~180MB
- 🐳 Suporte a 3 bancos via Docker Compose
- 🔧 8 variáveis de ambiente configuráveis
- 📊 Cobertura de testes mantida: 74%

**Configuração via ENV:**
- `DB_URL` - JDBC connection string
- `DB_USERNAME` - Database user (read-only)
- `DB_PASSWORD` - Database password
- `DB_DRIVER` - JDBC driver class
- `DB_TYPE` - Database type (PostgreSQL, MySQL, SQLServer)
- `SERVER_PORT` - Server port (default: 8080)
- `CACHE_ENABLED` - Enable caching (default: true)
- `LOGGING_LEVEL_*` - Logging configuration

---

### ✅ v0.1.0 - COMPLETO (28/Out/2024)

**Tema**: MVP + Tokenização + Métricas

**Entregas Principais:**
- ✅ Protocolo MCP JSON-RPC 2.0 completo
- ✅ 4 ferramentas MCP (schema, tables, triggers, query)
- ✅ Suporte multi-banco (PostgreSQL, MySQL, Oracle, MSSQL)
- ✅ Sistema de tokenização com métricas detalhadas
- ✅ Estimativa de custos de API (GPT-4 baseline)
- ✅ Performance metrics por ferramenta
- ✅ Teste end-to-end completo (jornada completa)
- ✅ 31 testes automatizados (100% passando)
- ✅ Cobertura de testes: 74%
- ✅ CI/CD com GitHub Actions
- ✅ Documentação completa (README, QUICKSTART, guides)

**Métricas Alcançadas:**
- ⏱️ Tempo médio de resposta: < 100ms
- 📊 Cobertura de código: 74% (instruction), 72% (branch)
- 🧪 Taxa de sucesso de testes: 100%
- 📦 Artefatos: JAR executável + relatório JaCoCo

**Limitações Conhecidas:**
- ⚠️ Sem autenticação/autorização
- ⚠️ Sem rate limiting
- ⚠️ Cache apenas em memória (não distribuído)
- ⚠️ Tokenização local (sem integração com APIs LLM reais)
- ⚠️ Sem suporte a múltiplas conexões de datasource simultâneas
- ⚠️ Sem geração de SQL via LLM (Text-to-SQL)

---

## ✅ Fase Completa - v0.2.0: Containerização e Deploy

**Objetivo**: Criar container Docker e publicar no Docker Hub para fácil deployment  
**Prioridade**: 🔴 CRÍTICA  
**Prazo**: 1-2 semanas  
**Status**: ✅ COMPLETO (Implementado em 28/Out/2024)  
**Meta de Cobertura**: 78%

### 2.1 Docker Container e Docker Hub 🐳 ✅ COMPLETO
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Tempo Estimado**: 3-5 dias  
**Status**: ✅ **IMPLEMENTADO**

**Implementações Realizadas:**

1. ✅ **Dockerfile Multi-Stage Otimizado**
   - Build stage com Maven
   - Runtime stage com JRE Alpine
   - Tamanho final: ~180MB
   - Multi-architecture: amd64 + arm64

2. ✅ **Script de Entrypoint Inteligente**
   - `docker-entrypoint.sh` com auto-detecção de drivers
   - Configuração dinâmica via ENV vars
   - Suporte a 4 bancos: PostgreSQL, MySQL, SQL Server, Oracle

3. ✅ **GitHub Actions - CI/CD Completo**
   - **Workflow CI**: Build Docker em todo commit
   - **Workflow Release**: Build + Push no Docker Hub em tags REL-*
   - Publicação automática com múltiplas tags
   - Suporte a multi-arquitetura via buildx

4. ✅ **Documentação Completa**
   - `DOCKER_README.md` - Para Docker Hub
   - `DOCKER_HUB_SETUP.md` - Guia de configuração de secrets
   - Exemplos de uso para cada banco
   - Docker Compose completos

**Variáveis de Ambiente Suportadas:**
```bash
DB_URL              # JDBC connection URL (required)
DB_USERNAME         # Database username (required)
DB_PASSWORD         # Database password (required)
DB_TYPE             # PostgreSQL|MySQL|SQLServer|Oracle (optional, auto-detect)
SERVER_PORT         # Default: 8080
CACHE_ENABLED       # Default: true
SPRING_PROFILES_ACTIVE  # Default: docker
```

**Tags Docker Hub:**
```
flaviomagacho/aitosql:latest
flaviomagacho/aitosql:X.Y.Z
flaviomagacho/aitosql:vX.Y.Z
```

**Testes Realizados:**
- ✅ Build local bem-sucedido
- ✅ Multi-stage funcionando
- ✅ Variáveis de ambiente funcionais
- ✅ Auto-detecção de drivers
- ✅ Healthcheck endpoint
- ✅ GitHub Actions CI passou
- ⏳ Aguardando primeira release para validar push no Docker Hub

**Próximo Passo:**
- Criar tag `REL-0.3.0` para testar publicação no Docker Hub  
**Estimativa**: 1 semana  
**Status**: ✅ COMPLETO

#### Tarefas:
- [x] **Dockerfile Multi-Stage** (2 dias)
  - ✅ Build stage com Maven
  - ✅ Runtime stage com JRE 17 (alpine slim)
  - ✅ Otimização de layers para cache
  - ✅ Health check endpoint
  - ✅ Testes de build local
  
- [x] **Configuração Parametrizada via ENV** (2 dias)
  - ✅ Parâmetros de conexão via variáveis de ambiente:
    - `DB_URL` - URL JDBC (ex: jdbc:postgresql://host:5432/db)
    - `DB_USERNAME` - Usuário do banco (read-only recomendado)
    - `DB_PASSWORD` - Senha do banco
    - `DB_DRIVER` - Driver JDBC (ex: org.postgresql.Driver)
    - `DB_TYPE` - Tipo do banco (PostgreSQL, MySQL, Oracle, MSSQL)
    - `SERVER_PORT` - Porta do servidor (default: 8080)
    - `CACHE_ENABLED` - Habilitar cache (default: true)
  - ✅ application.properties com suporte a ENV vars
  - ✅ application-docker.properties para profile Docker
  - ✅ Testes com diferentes bancos (PostgreSQL, MySQL, SQL Server)
  
- [x] **Docker Compose para Testes Locais** (1 dia)
  - ✅ docker-compose-postgres.yml com MCP Server + PostgreSQL
  - ✅ docker-compose-mysql.yml com MCP Server + MySQL
  - ✅ docker-compose-sqlserver.yml com MCP Server + SQL Server
  - ✅ Scripts de inicialização de banco (schema de exemplo)
  - ✅ Volume mounting para persistência
  
- [x] **Publicação no Docker Hub** (1 dia)
  - ✅ Repositório configurado: `flaviomagacho/aitosql`
  - ✅ Tags semânticas: `latest`, `0.2.0`, `0.2`, `0`
  - ✅ GitHub Actions para build e push automático (.github/workflows/docker-build.yml)
  - ✅ Multi-architecture support (amd64, arm64)
  - ✅ DOCKER_README.md detalhado para Docker Hub
  
- [x] **Suporte Multi-Database Drivers** (1 dia)
  - ✅ Drivers incluídos no pom.xml: PostgreSQL, MySQL, SQL Server
  - ✅ Configuração dinâmica via DB_DRIVER e DB_TYPE
  - ✅ Scripts de inicialização para cada banco
  - ✅ Docker Compose para cada banco suportado

#### Deliverables:
- ✅ Dockerfile otimizado multi-stage (< 200MB)
- ✅ docker-compose.yml para cada banco suportado (PostgreSQL, MySQL, SQL Server)
- ✅ Configuração pronta para publicação no Docker Hub: `flaviomagacho/aitosql`
- ✅ GitHub Actions para CI/CD de imagens Docker
- ✅ Documentação completa:
  - ✅ DOCKER_DEPLOYMENT.md (guia de deployment)
  - ✅ DOCKER_README.md (para Docker Hub)
  - ✅ DOCKER_BUILD_GUIDE.md (guia de build e publicação)
- ✅ Scripts automatizados:
  - ✅ test-docker-deployment.sh (testes automatizados)
  - ✅ docker-build-and-push.sh (build e publicação)
- ✅ Arquivos de inicialização:
  - ✅ docker/postgres/init.sql
  - ✅ docker/mysql/init.sql
  - ✅ docker/sqlserver/init.sql

**Impacto na Cobertura**: Mantém 74% (infraestrutura, sem código novo de negócio)

#### Exemplo de Uso:
```bash
# PostgreSQL
docker run -d \
  -e DB_URL="jdbc:postgresql://postgres:5432/mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_password" \
  -e DB_TYPE="PostgreSQL" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest

# MySQL
docker run -d \
  -e DB_URL="jdbc:mysql://mysql:3306/mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_password" \
  -e DB_TYPE="MySQL" \
  -e DB_DRIVER="com.mysql.cj.jdbc.Driver" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest

# SQL Server
docker run -d \
  -e DB_URL="jdbc:sqlserver://sqlserver:1433;databaseName=mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_password" \
  -e DB_TYPE="SQLServer" \
  -e DB_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest

# Com Docker Compose (recomendado para desenvolvimento)
docker-compose -f docker-compose-postgres.yml up -d
docker-compose -f docker-compose-mysql.yml up -d
docker-compose -f docker-compose-sqlserver.yml up -d
```

---

## 🤖 Próxima Fase - v0.3.0: Integração com LLMs Reais

**Objetivo**: Conectar com APIs de LLMs para Text-to-SQL e análise inteligente  
**Prioridade**: 🟡 ALTA  
**Prazo**: 3-4 semanas  
**Status**: ⏳ PLANEJADO  
**Meta de Cobertura**: 82%

### 3.1 Integração com APIs de LLMs 🤖
**Prioridade**: ALTA  
**Estimativa**: 2 semanas

#### Tarefas:
- [ ] **OpenAI GPT-4 Integration** (5 dias)
  - Implementar client para API OpenAI
  - Tokenização real usando tiktoken equivalente (Java)
  - Geração de SQL a partir de linguagem natural
  - Cache de respostas para reduzir custos
  - Tracking de custos real (não estimado)
  - Testes de integração (mock + real)
  
- [ ] **Anthropic Claude Integration** (3 dias)
  - Client para API Claude
  - Suporte a Claude 3.5 Sonnet
  - Comparação de performance vs GPT-4
  - Testes de integração
  
- [ ] **Google Gemini Integration** (2 dias)
  - Client para API Gemini
  - Suporte a Gemini Pro/Ultra
  - Testes de integração
  
- [ ] **Ollama Local LLM** (2 dias)
  - Integração com Ollama para modelos locais
  - Suporte a Llama 3, Mistral, CodeLlama
  - Sem custos de API
  - Testes locais

#### Deliverables:
- ✅ Interface abstrata `LLMProvider`
- ✅ 4 implementações concretas (OpenAI, Claude, Gemini, Ollama)
- ✅ Service layer: `LLMService` com seleção de provider
- ✅ Configuração via properties (provider, api-key, model)
- ✅ Testes unitários + integração (mock)
- ✅ Documentação de setup para cada provider

**Impacto na Cobertura**: +2%

---

### 3.2 Text-to-SQL Intelligence 🧠
**Prioridade**: ALTA  
**Estimativa**: 1.5 semanas

#### Tarefas:
- [ ] **Nova Ferramenta MCP: `naturalLanguageQuery`** (4 dias)
  - Parser de intenção de usuário (extrair entidades, ação)
  - Geração de SQL usando LLM + contexto de schema
  - Prompt engineering otimizado para SQL generation
  - Validação e sanitização de SQL gerado
  - Refinamento iterativo (se SQL inválido, retry com erro)
  - Testes com diversos tipos de perguntas
  
- [ ] **Nova Ferramenta MCP: `explainQuery`** (2 dias)
  - Recebe SQL, retorna explicação em linguagem natural
  - Usa LLM para gerar explicação clara
  - Útil para documentação automática
  - Testes com queries complexas
  
- [ ] **Query Optimization Suggestions** (2 dias)
  - Nova ferramenta: `suggestQueryOptimizations`
  - LLM analisa EXPLAIN plan do banco
  - Sugestões de índices, reescrita, hints
  - Testes com queries lentas

#### Deliverables:
- ✅ 3 novas ferramentas MCP
- ✅ Prompt templates versionados (v1.0)
- ✅ Endpoint `/mcp/tools/call` atualizado
- ✅ Testes E2E de Text-to-SQL
- ✅ Documentação de prompts e accuracy
- ✅ Comparação de accuracy entre LLMs (OpenAI vs Claude vs Gemini)

**Impacto na Cobertura**: +2%

---

### 3.3 Cost Tracking Dashboard 💰
**Prioridade**: MÉDIA  
**Estimativa**: 1 semana

#### Tarefas:
- [ ] **Enhanced Cost Analytics** (3 dias)
  - Endpoint `/mcp/analytics/cost`
  - Breakdown por ferramenta, LLM provider, período (dia/semana/mês)
  - Comparação de custos: OpenAI vs Claude vs Gemini
  - Alertas de threshold de custo (configurável)
  - Export CSV/JSON
  
- [ ] **Simple Web Dashboard** (2 dias)
  - HTML + Chart.js simples (sem framework pesado)
  - Endpoint `/mcp/dashboard` serve página estática
  - Gráficos de custos ao longo do tempo
  - Top queries por custo
  - Cache hit rate visualization
  
- [ ] **Usage Reporting** (2 dias)
  - Relatórios automáticos (JSON)
  - Endpoint `/mcp/reports/usage?from=X&to=Y`
  - Integração com billing systems (webhook opcional)

#### Deliverables:
- ✅ Enhanced metrics endpoints
- ✅ Simple web dashboard (HTML + Chart.js)
- ✅ Automated reporting
- ✅ Testes de analytics
- ✅ Documentação de uso

**Impacto na Cobertura**: +0%

---

## 🔒 Próxima Fase - v0.4.0: Segurança e Produção

**Objetivo**: Tornar o servidor production-ready com auth, rate limiting e compliance  
**Prioridade**: 🟢 MÉDIA  
**Prazo**: 2-3 semanas  
**Status**: ⏳ PLANEJADO  
**Meta de Cobertura**: 86%

### 4.1 Autenticação e Autorização 🔐
**Prioridade**: CRÍTICA  
**Estimativa**: 1 semana

#### Tarefas:
- [ ] **API Key Authentication** (3 dias)
  - Implementação mais simples que JWT
  - Header: `X-API-Key`
  - Gestão de keys via endpoints admin
  - Validação em filter/interceptor
  - Testes de segurança (401, 403)
  
- [ ] **JWT Authentication** (opcional, 2 dias)
  - Spring Security configuration
  - JWT token generation/validation
  - Endpoint `/auth/login` e `/auth/refresh`
  - User/password storage (in-memory inicial)
  
- [ ] **Role-Based Access Control (RBAC)** (2 dias)
  - Roles: ADMIN, USER, READONLY
  - Permissões por ferramenta MCP
  - Audit log de acessos negados
  - Testes de RBAC

#### Deliverables:
- ✅ Autenticação funcional (API Key como padrão)
- ✅ Documentação de setup de auth
- ✅ Testes de segurança
- ✅ Migration guide (sem auth → com auth)

**Impacto na Cobertura**: +2%

---

### 4.2 Rate Limiting e Throttling 🚦
**Prioridade**: ALTA  
**Estimativa**: 4 dias

#### Tarefas:
- [ ] **Request Rate Limiting** (2 dias)
  - Bucket4j implementation
  - Limites configuráveis por endpoint
  - Headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
  - Response HTTP 429 quando exceder
  - Testes de rate limiting
  
- [ ] **Cost-Based Throttling** (2 dias)
  - Rate limit baseado em custo de LLM (não só requests)
  - Exemplo: máx $10/dia por usuário/API key
  - Diferentes limits por tier (free: $1/day, pro: $100/day)
  - Testes de cost throttling
  
- [ ] **Query Timeout** (já existe parcialmente)
  - Timeout configurável para queries longas
  - Cancelamento automático após X segundos
  - Métricas de queries timeouts

#### Deliverables:
- ✅ Rate limiting funcional
- ✅ Cost-based throttling
- ✅ Configuração via properties
- ✅ Testes de throttling
- ✅ Documentação

**Impacto na Cobertura**: +2%

---

### 4.3 Audit Logging e Compliance 📝
**Prioridade**: MÉDIA  
**Estimativa**: 1 semana

#### Tarefas:
- [ ] **Comprehensive Audit Logs** (3 dias)
  - Registrar todas as queries executadas
  - Timestamp, user/API key, query, resultado (success/fail)
  - Tentativas de SQL injection
  - Acessos negados (401, 403)
  - Custos de LLM por requisição
  - Log separado (audit.log)
  
- [ ] **Log Export e Análise** (2 dias)
  - Export para CSV/JSON
  - Endpoint `/admin/audit?from=X&to=Y`
  - Filtros: user, tool, status
  
- [ ] **LGPD/GDPR Compliance Basics** (2 dias)
  - Data masking para dados sensíveis (opcional)
  - Anonimização de logs
  - Endpoint `/admin/user/{id}/purge` (direito ao esquecimento)

#### Deliverables:
- ✅ Sistema de audit logging
- ✅ Endpoints de export
- ✅ Documentação de compliance
- ✅ Testes de logging

**Impacto na Cobertura**: +0% (logging não aumenta cobertura significativamente)

---

## ⚡ Futuro - v0.5.0: Performance e Escalabilidade

**Objetivo**: Otimizar para alta carga e múltiplos tenants  
**Prioridade**: 🟢 MÉDIA  
**Prazo**: 2-3 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 90%

### Resumo de Features:

#### 5.1 Cache Distribuído (Redis)
- Migrar de Caffeine para Redis
- Cache de schema, query results, respostas LLM
- Smart invalidation
- Multi-layer caching (L1: local, L2: Redis)

#### 5.2 Async Processing
- Queries longas em background
- WebSocket streaming de resultados
- Message queue (RabbitMQ/Kafka)

#### 5.3 Multi-Tenancy
- Isolamento por tenant
- Dynamic datasource management
- Tenant-specific settings (LLM provider, budget, rate limits)

**Estimativa Total**: 2-3 semanas  
**Impacto na Cobertura**: +4%

---

## 📊 Futuro - v0.6.0: Observabilidade Avançada

**Objetivo**: Monitoring, metrics e alertas production-grade  
**Prioridade**: 🟢 MÉDIA  
**Prazo**: 1-2 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 92%

### Resumo de Features:

#### 6.1 Prometheus & Grafana
- Endpoint `/actuator/prometheus`
- Dashboards Grafana (overview, custos, performance, segurança)
- Custom metrics (queries/min, custo/hora, cache hit rate)

#### 6.2 Distributed Tracing
- Zipkin/Jaeger integration
- OpenTelemetry support
- End-to-end trace de requisições

#### 6.3 Alertas e SLOs
- Alertas via Slack/Email/PagerDuty
- SLO definition (uptime, latência, error rate)
- Anomaly detection básica

**Estimativa Total**: 1-2 semanas  
**Impacto na Cobertura**: +2%

---

## 🎨 Futuro - v0.7.0: Developer Experience

**Objetivo**: Facilitar integração com SDKs, CLI e Web UI  
**Prioridade**: 🔵 BAIXA  
**Prazo**: 3-4 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 94%

### Resumo de Features:

#### 7.1 SDKs e Clients
- Python SDK (PyPI)
- TypeScript/JavaScript SDK (NPM)
- Go Client
- CLI Tool (brew, apt, chocolatey)

#### 7.2 Web Dashboard
- Schema Explorer UI (React + TypeScript)
- SQL Query Builder
- Natural Language chat interface
- Monitoring dashboard

#### 7.3 Documentação Interativa
- OpenAPI/Swagger UI
- Interactive tutorials
- Code examples library

**Estimativa Total**: 3-4 semanas  
**Impacto na Cobertura**: +2%

---

## 🎯 Timeline Visual

```
2024 Q4 (Atual)
│
├─ Oct 28 ✅ v0.1.0: MVP + Tokenização + Métricas
│
├─ Nov 2024 🔄 v0.2.0: Docker Container + Docker Hub
│   ├─ Week 1: Dockerfile multi-stage + ENV config
│   └─ Week 2: Docker Compose + Docker Hub publish + Multi-DB drivers

2024 Q4/2025 Q1
│
├─ Dec 2024 ⏳ v0.3.0: Integração LLMs (OpenAI, Claude, Gemini, Ollama)
│   ├─ Week 1: OpenAI GPT-4 integration
│   ├─ Week 2: Claude, Gemini, Ollama
│   ├─ Week 3: Text-to-SQL (naturalLanguageQuery, explainQuery)
│   └─ Week 4: Cost dashboard
│
├─ Jan 2025 ⏳ v0.4.0: Segurança (Auth, Rate Limiting, Audit)
│   ├─ Week 1: API Key auth + RBAC
│   ├─ Week 2: Rate limiting + Cost throttling
│   └─ Week 3: Audit logging + Compliance
│
├─ Feb 2025 🔮 v0.5.0: Performance (Redis, Async, Multi-tenancy)
│   ├─ Week 1: Redis cache
│   ├─ Week 2: Async processing
│   └─ Week 3-4: Multi-tenancy
│
├─ Mar 2025 🔮 v0.6.0: Observabilidade (Prometheus, Grafana, Alertas)
│   └─ 1-2 weeks
│
└─ Apr 2025 🔮 v0.7.0: Developer Experience (SDKs, CLI, Web UI)
    └─ 3-4 weeks
```

---

## 📊 Métricas de Sucesso

### v0.1.0 ✅ ALCANÇADO
- ✅ Cobertura de testes: 74%
- ✅ 31 testes (100% passando)
- ✅ Tempo médio < 100ms
- ✅ Tokenização implementada
- ✅ Teste E2E completo

### v0.2.0 ✅ ALCANÇADO
- ✅ Cobertura: 74% (mantida)
- ✅ Imagem Docker < 200MB (alcançado: ~180MB)
- ✅ Suporte a 3 bancos (PostgreSQL, MySQL, SQL Server)
- ✅ Docker Compose funcional para cada banco
- ✅ Configuração pronta para Docker Hub: `flaviomagacho/aitosql`
- ✅ Configuração 100% via ENV vars
- ✅ GitHub Actions para CI/CD automático
- ✅ Multi-architecture support (amd64, arm64)
- ✅ Scripts de teste e deploy automatizados

### v0.3.0 🎯 METAS
- 🎯 Cobertura: 82%
- 🎯 Integração com 4 LLM providers
- 🎯 Text-to-SQL accuracy > 80%
- 🎯 Tempo médio < 150ms (inclui chamadas LLM)
- 🎯 Cost tracking real implementado

### v0.4.0 🎯 METAS
- 🎯 Cobertura: 86%
- 🎯 Autenticação funcional
- 🎯 Rate limiting: 100 req/min/user
- 🎯 Audit log completo
- 🎯 Zero vulnerabilidades críticas

### v0.5.0 🎯 METAS
- 🎯 Cobertura: 90%
- 🎯 Cache hit rate > 60%
- 🎯 Suporte a 10+ tenants
- 🎯 Async queries funcionais

### v0.6.0 🎯 METAS
- 🎯 Cobertura: 92%
- 🎯 Uptime > 99.9%
- 🎯 Latência p95 < 200ms
- 🎯 Prometheus + Grafana dashboards

### v0.7.0 🎯 METAS
- 🎯 Cobertura: 94%
- 🎯 3+ SDKs publicados
- 🎯 CLI tool funcional
- 🎯 Web UI completa

---

## 💡 Backlog de Ideias (Sem Prazo)

Funcionalidades interessantes para o futuro distante:

- [ ] GraphQL API (além de JSON-RPC)
- [ ] Data Masking/Anonymization avançado
- [ ] Query Cost Estimation (antes de executar)
- [ ] Integration Testing Framework
- [ ] Marketplace de Tools MCP customizadas
- [ ] Real-time collaboration (múltiplos usuários)
- [ ] Plugin system para extensibilidade
- [ ] Mobile app para monitoramento
- [ ] VS Code extension
- [ ] Jupyter kernel para data science
- [ ] Suporte a bancos NoSQL (MongoDB, Cassandra)
- [ ] Suporte a Data Lakes (S3, BigQuery)
- [ ] Query suggestion baseado em histórico

---

## 🤝 Como Contribuir

### Áreas que Precisam de Ajuda:
1. **LLM Integration**: Testar accuracy de diferentes modelos
2. **Frontend**: Dashboard React/Vue
3. **SDKs**: Python, JavaScript, Go clients
4. **Testes**: Aumentar cobertura para 90%+
5. **Documentação**: Tutoriais e exemplos
6. **DevOps**: Kubernetes manifests, Helm charts

### Para Começar:
```bash
# 1. Fork o projeto
# 2. Clone seu fork
git clone https://github.com/SEU_USUARIO/aiToSql.git

# 3. Criar branch
git checkout -b feature/minha-feature

# 4. Fazer mudanças e testar
mvn clean test

# 5. Verificar cobertura
mvn jacoco:report

# 6. Commit e push
git commit -m "feat: adicionar nova feature"
git push origin feature/minha-feature

# 7. Abrir Pull Request
```

---

## 📞 Feedback e Discussões

**Issues**: https://github.com/magacho/aiToSql/issues  
**Discussions**: https://github.com/magacho/aiToSql/discussions  
**Mantenedor**: @magacho

---

## 🔄 Processo de Revisão do Roadmap

Este roadmap é revisado:
- ✅ **Após cada release** (ajustar prioridades)
- ✅ **Mensalmente** (adicionar/remover features)
- ✅ **Com feedback da comunidade** (issues, discussions)

**Última atualização**: 28 de Outubro de 2024  
**Próxima revisão**: Após release v0.2.0  
**Versão do documento**: 2.0
