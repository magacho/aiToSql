# 📋 Tarefas Pendentes - aiToSql MCP Server

**Data**: 29 de Outubro de 2024  
**Status Atual**: v0.2.0 (Docker publicado)  
**Próxima Release**: v0.3.0

---

## ✅ Completado

### Fase 1 - MVP (v0.1.0)
- ✅ Protocolo MCP JSON-RPC 2.0
- ✅ 4 ferramentas MCP implementadas
- ✅ Sistema de tokenização
- ✅ Métricas de performance
- ✅ Testes automatizados (74% cobertura)
- ✅ CI/CD com GitHub Actions

### Fase 2.1 - Docker (v0.2.0)
- ✅ Dockerfile multi-stage otimizado
- ✅ Configuração via variáveis de ambiente
- ✅ Suporte multi-database (PostgreSQL, MySQL, SQL Server)
- ✅ Docker Compose para testes locais
- ✅ GitHub Actions para build e publicação
- ✅ Multi-architecture (amd64, arm64)
- ✅ Documentação completa

---

## 🎯 Fase Atual - v0.3.0: Integração com LLMs Reais

**Objetivo**: Conectar com APIs de LLMs para Text-to-SQL e análise inteligente  
**Prioridade**: 🔴 CRÍTICA  
**Prazo Estimado**: 3-4 semanas  
**Meta de Cobertura**: 82%

### 3.1 Integração com APIs de LLMs 🤖
**Status**: ⏳ PLANEJADO  
**Estimativa**: 2 semanas

#### Tarefas:

- [ ] **OpenAI GPT-4 Integration** (5 dias)
  - [ ] Implementar client para API OpenAI
  - [ ] Tokenização real usando tiktoken equivalente (Java)
  - [ ] Geração de SQL a partir de linguagem natural
  - [ ] Cache de respostas para reduzir custos
  - [ ] Tracking de custos real (não estimado)
  - [ ] Testes de integração (mock + real)
  - [ ] Configuração via properties: `openai.api-key`, `openai.model`
  
- [ ] **Anthropic Claude Integration** (3 dias)
  - [ ] Client para API Claude
  - [ ] Suporte a Claude 3.5 Sonnet
  - [ ] Comparação de performance vs GPT-4
  - [ ] Testes de integração
  - [ ] Configuração via properties: `claude.api-key`, `claude.model`
  
- [ ] **Google Gemini Integration** (2 dias)
  - [ ] Client para API Gemini
  - [ ] Suporte a Gemini Pro/Ultra
  - [ ] Testes de integração
  - [ ] Configuração via properties: `gemini.api-key`, `gemini.model`
  
- [ ] **Ollama Local LLM** (2 dias)
  - [ ] Integração com Ollama para modelos locais
  - [ ] Suporte a Llama 3, Mistral, CodeLlama
  - [ ] Sem custos de API
  - [ ] Testes locais
  - [ ] Configuração via properties: `ollama.base-url`, `ollama.model`

#### Arquitetura:

```java
// Interface abstrata
public interface LLMProvider {
    String generateSQL(String naturalLanguageQuery, String schemaContext);
    String explainQuery(String sqlQuery);
    TokenizationMetrics getTokenMetrics();
}

// Implementações
- OpenAIProvider implements LLMProvider
- ClaudeProvider implements LLMProvider  
- GeminiProvider implements LLMProvider
- OllamaProvider implements LLMProvider

// Service Layer
@Service
public class LLMService {
    private final Map<String, LLMProvider> providers;
    
    public String generateSQL(String query, String schema, String provider) {
        return providers.get(provider).generateSQL(query, schema);
    }
}
```

#### Configuração (application.properties):

```properties
# LLM Provider Selection
llm.provider=openai  # openai|claude|gemini|ollama

# OpenAI
llm.openai.api-key=${OPENAI_API_KEY}
llm.openai.model=gpt-4-turbo-preview
llm.openai.base-url=https://api.openai.com/v1

# Claude
llm.claude.api-key=${CLAUDE_API_KEY}
llm.claude.model=claude-3-5-sonnet-20241022
llm.claude.base-url=https://api.anthropic.com/v1

# Gemini
llm.gemini.api-key=${GEMINI_API_KEY}
llm.gemini.model=gemini-pro
llm.gemini.base-url=https://generativelanguage.googleapis.com/v1

# Ollama (local)
llm.ollama.base-url=http://localhost:11434
llm.ollama.model=llama3
```

#### Deliverables:
- ✅ Interface `LLMProvider`
- ✅ 4 implementações concretas
- ✅ Service layer: `LLMService` com seleção de provider
- ✅ Configuração via properties
- ✅ Testes unitários + integração (mock)
- ✅ Documentação de setup para cada provider

**Impacto na Cobertura**: +2%

---

### 3.2 Text-to-SQL Intelligence 🧠
**Status**: ⏳ PLANEJADO  
**Estimativa**: 1.5 semanas

#### Tarefas:

- [ ] **Nova Ferramenta MCP: `naturalLanguageQuery`** (4 dias)
  - [ ] Parser de intenção de usuário
  - [ ] Geração de SQL usando LLM + contexto de schema
  - [ ] Prompt engineering otimizado para SQL generation
  - [ ] Validação e sanitização de SQL gerado
  - [ ] Refinamento iterativo (se SQL inválido, retry com erro)
  - [ ] Testes com diversos tipos de perguntas
  - [ ] Documentação de accuracy
  
- [ ] **Nova Ferramenta MCP: `explainQuery`** (2 dias)
  - [ ] Recebe SQL, retorna explicação em linguagem natural
  - [ ] Usa LLM para gerar explicação clara
  - [ ] Útil para documentação automática
  - [ ] Testes com queries complexas
  
- [ ] **Query Optimization Suggestions** (2 dias)
  - [ ] Nova ferramenta: `suggestQueryOptimizations`
  - [ ] LLM analisa EXPLAIN plan do banco
  - [ ] Sugestões de índices, reescrita, hints
  - [ ] Testes com queries lentas

#### Exemplo de Implementação:

```java
@Service
public class TextToSQLService {
    
    private final LLMService llmService;
    private final SchemaService schemaService;
    private final QueryService queryService;
    
    public QueryResult naturalLanguageQuery(String question, String provider) {
        // 1. Obter contexto do schema
        String schema = schemaService.getSchemaStructure();
        
        // 2. Gerar SQL via LLM
        String sql = llmService.generateSQL(question, schema, provider);
        
        // 3. Validar SQL
        if (!isValidSQL(sql)) {
            throw new IllegalArgumentException("SQL inválido gerado");
        }
        
        // 4. Executar query
        List<Map<String, Object>> results = queryService.executeSearch(sql);
        
        // 5. Gerar explicação
        String explanation = llmService.explainQuery(sql);
        
        return new QueryResult(sql, results, explanation);
    }
    
    public String explainQuery(String sql) {
        return llmService.explainQuery(sql);
    }
    
    public List<String> suggestOptimizations(String sql) {
        // Executar EXPLAIN no banco
        String explainPlan = queryService.getExplainPlan(sql);
        
        // LLM analisa e sugere
        return llmService.suggestOptimizations(sql, explainPlan);
    }
}
```

#### Novos Endpoints MCP:

```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "naturalLanguageQuery",
    "arguments": {
      "question": "Quais são os 10 clientes com mais pedidos?",
      "provider": "openai",
      "maxRows": 10
    }
  }
}
```

#### Deliverables:
- ✅ 3 novas ferramentas MCP
- ✅ Prompt templates versionados (v1.0)
- ✅ Endpoint `/mcp/tools/call` atualizado
- ✅ Testes E2E de Text-to-SQL
- ✅ Documentação de prompts e accuracy
- ✅ Comparação de accuracy entre LLMs

**Impacto na Cobertura**: +2%

---

### 3.3 Cost Tracking Dashboard 💰
**Status**: ⏳ PLANEJADO  
**Estimativa**: 1 semana

#### Tarefas:

- [ ] **Enhanced Cost Analytics** (3 dias)
  - [ ] Endpoint `/mcp/analytics/cost`
  - [ ] Breakdown por ferramenta, LLM provider, período
  - [ ] Comparação de custos: OpenAI vs Claude vs Gemini
  - [ ] Alertas de threshold de custo (configurável)
  - [ ] Export CSV/JSON
  
- [ ] **Simple Web Dashboard** (2 dias)
  - [ ] HTML + Chart.js simples (sem framework pesado)
  - [ ] Endpoint `/mcp/dashboard` serve página estática
  - [ ] Gráficos de custos ao longo do tempo
  - [ ] Top queries por custo
  - [ ] Cache hit rate visualization
  
- [ ] **Usage Reporting** (2 dias)
  - [ ] Relatórios automáticos (JSON)
  - [ ] Endpoint `/mcp/reports/usage?from=X&to=Y`
  - [ ] Integração com billing systems (webhook opcional)

#### Exemplo de Response:

```json
{
  "period": {
    "from": "2024-10-01",
    "to": "2024-10-31"
  },
  "totalCost": {
    "usd": 125.50
  },
  "breakdown": {
    "openai": {
      "calls": 1234,
      "tokens": 1500000,
      "cost": 75.00
    },
    "claude": {
      "calls": 567,
      "tokens": 800000,
      "cost": 40.00
    },
    "gemini": {
      "calls": 234,
      "tokens": 300000,
      "cost": 10.50
    }
  },
  "topExpensiveQueries": [
    {
      "query": "Análise complexa de vendas...",
      "cost": 5.25,
      "tokens": 35000
    }
  ]
}
```

#### Deliverables:
- ✅ Enhanced metrics endpoints
- ✅ Simple web dashboard (HTML + Chart.js)
- ✅ Automated reporting
- ✅ Testes de analytics
- ✅ Documentação de uso

**Impacto na Cobertura**: +0%

---

## 🔒 Fase Futura - v0.4.0: Segurança e Produção

**Objetivo**: Tornar o servidor production-ready  
**Prioridade**: 🟡 ALTA  
**Prazo Estimado**: 2-3 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 86%

### Resumo:
- Autenticação e Autorização (API Key + JWT)
- Rate Limiting e Cost-Based Throttling
- Audit Logging e Compliance (LGPD/GDPR)

---

## ⚡ Fase Futura - v0.5.0: Performance e Escalabilidade

**Objetivo**: Otimizar para alta carga  
**Prioridade**: 🟡 MÉDIA  
**Prazo Estimado**: 2-3 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 90%

### Resumo:
- Cache Distribuído (Redis)
- Async Processing (WebSocket streaming)
- Multi-Tenancy

---

## 📊 Fase Futura - v0.6.0: Observabilidade Avançada

**Objetivo**: Monitoring e metrics production-grade  
**Prioridade**: 🟢 MÉDIA  
**Prazo Estimado**: 1-2 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 92%

### Resumo:
- Prometheus & Grafana
- Distributed Tracing (Zipkin/Jaeger)
- Alertas e SLOs

---

## 🎨 Fase Futura - v0.7.0: Developer Experience

**Objetivo**: SDKs, CLI e Web UI  
**Prioridade**: 🔵 BAIXA  
**Prazo Estimado**: 3-4 semanas  
**Status**: 🔮 FUTURO  
**Meta de Cobertura**: 94%

### Resumo:
- SDKs (Python, TypeScript/JS, Go)
- CLI Tool
- Web Dashboard React
- Documentação interativa (Swagger UI)

---

## 🎯 Timeline

```
Nov 2024 🔄 v0.3.0: Integração LLMs
│   ├─ Week 1: OpenAI + Claude
│   ├─ Week 2: Gemini + Ollama
│   ├─ Week 3: Text-to-SQL tools
│   └─ Week 4: Cost dashboard

Dec 2024 ⏳ v0.4.0: Segurança
│   ├─ Week 1: Auth + RBAC
│   ├─ Week 2: Rate limiting
│   └─ Week 3: Audit logging

Jan 2025 🔮 v0.5.0: Performance
Feb 2025 🔮 v0.6.0: Observabilidade
Mar 2025 🔮 v0.7.0: Developer Experience
```

---

## 📈 Métricas de Sucesso

### v0.3.0 🎯 METAS
- 🎯 Cobertura: 82%
- 🎯 Integração com 4 LLM providers
- 🎯 Text-to-SQL accuracy > 80%
- 🎯 Tempo médio < 2s (inclui chamadas LLM)
- 🎯 Cost tracking real implementado
- 🎯 Dashboard web funcional

---

## 🚀 Próximos Passos Imediatos

1. **Escolher LLM Provider Inicial** (Sugestão: OpenAI ou Ollama)
2. **Implementar Interface LLMProvider**
3. **Implementar OpenAI Client**
4. **Criar Tool `naturalLanguageQuery`**
5. **Adicionar testes de integração**
6. **Documentar setup e exemplos**

---

**Nota**: Kubernetes deployment (Fase 2.2) foi **removido** do roadmap conforme solicitação do usuário.

**Última Atualização**: 29 de Outubro de 2024  
**Versão do Documento**: 1.0
