# 🚀 Próximos Passos - aiToSql MCP Server

## Status Atual

✅ **v0.1.0 publicada** (28/Out/2024)  
✅ **31 testes passando (100%)**  
✅ **Cobertura: 74%**  
✅ **Tokenização local implementada**  
✅ **Teste E2E completo (jornada full-stack)**  
🔄 **Em desenvolvimento: v0.2.0**

---

## 🎯 Objetivo da v0.2.0: Integração com LLMs Reais

Atualmente, temos **estimativas** de tokens e custos baseadas em aproximações (1 token ≈ 4 chars). 

**Precisamos integrar com APIs reais de LLMs para:**
1. ✨ **Gerar SQL a partir de linguagem natural** (Text-to-SQL)
2. 🎯 **Tokenização precisa** (usando contadores reais dos LLMs)
3. 💰 **Custos reais** de API tracking
4. 🧠 **Query explanation** (SQL → Linguagem Natural)
5. 📊 **Query optimization suggestions**

---

## 📅 Cronograma v0.2.0 (3-4 Semanas)

### 🤖 Semana 1-2: Integração com APIs de LLMs

#### 1. OpenAI GPT-4 Integration (5 dias) 🔴 CRÍTICO

**Objetivos:**
- Implementar client HTTP para OpenAI API
- Tokenização real usando tiktoken-java (ou equivalente)
- Geração de SQL a partir de linguagem natural
- Cache de respostas para economizar custos
- Tracking de custos real (não estimado)

**Tasks:**
```bash
# Branch de trabalho
git checkout -b feature/openai-integration

# Adicionar dependência no pom.xml
# <dependency>
#   <groupId>com.theokanning.openai-gpt3-java</groupId>
#   <artifactId>service</artifactId>
#   <version>0.18.2</version>
# </dependency>

# Criar arquivos:
# - src/main/java/com/magacho/aiToSql/llm/LLMProvider.java (interface)
# - src/main/java/com/magacho/aiToSql/llm/OpenAIProvider.java (impl)
# - src/main/java/com/magacho/aiToSql/service/LLMService.java
# - src/test/java/com/magacho/aiToSql/llm/OpenAIProviderTest.java
```

**Entregáveis:**
- [ ] Interface `LLMProvider` com métodos:
  - `generateSQL(String naturalLanguage, String schemaContext)`
  - `explainQuery(String sqlQuery)`
  - `countTokens(String text)`
  - `estimateCost(int tokens)`
- [ ] `OpenAIProvider` implementado
- [ ] `LLMService` para orquestração
- [ ] Testes unitários (mock da API)
- [ ] Testes de integração (chamada real, opcional)
- [ ] Documentação: `docs/OPENAI_SETUP.md`

**Configuração (application.properties):**
```properties
# OpenAI Configuration
llm.provider=openai
llm.openai.api-key=${OPENAI_API_KEY}
llm.openai.model=gpt-4-turbo-preview
llm.openai.max-tokens=2000
llm.openai.temperature=0.0
```

---

#### 2. Claude, Gemini e Ollama (5 dias)

**Claude (Anthropic)** - 2 dias
- [ ] `ClaudeProvider` implementation
- [ ] Testes de integração
- [ ] Comparação de performance vs GPT-4

**Gemini (Google)** - 1 dia
- [ ] `GeminiProvider` implementation
- [ ] Testes básicos

**Ollama (Local)** - 2 dias
- [ ] `OllamaProvider` implementation
- [ ] Suporte a Llama 3, Mistral, CodeLlama
- [ ] Testes locais (sem custo)
- [ ] Documentação de setup do Ollama

**Configuração:**
```properties
# llm.provider=openai|claude|gemini|ollama

# Claude
llm.claude.api-key=${ANTHROPIC_API_KEY}
llm.claude.model=claude-3-5-sonnet-20241022

# Gemini
llm.gemini.api-key=${GOOGLE_API_KEY}
llm.gemini.model=gemini-1.5-pro

# Ollama (local, sem API key)
llm.ollama.base-url=http://localhost:11434
llm.ollama.model=llama3.2
```

---

### 🧠 Semana 3: Text-to-SQL Intelligence

#### 3. Nova Ferramenta MCP: `naturalLanguageQuery` (4 dias) 🔴 CRÍTICO

**Objetivo:** Permitir que usuários façam perguntas em linguagem natural e recebam SQL + resultados.

**Flow:**
```
1. Usuário envia: "Quais são os 10 maiores clientes por valor de compras?"
2. MCP chama getSchemaStructure (para contexto)
3. MCP chama LLMService.generateSQL(pergunta, schema)
4. LLM retorna: "SELECT c.name, SUM(o.total) FROM customers c..."
5. MCP valida e executa SQL
6. MCP retorna: SQL gerado + resultados + custo da operação
```

**Tasks:**
- [ ] Criar `NaturalLanguageQueryService`
- [ ] Adicionar ferramenta `naturalLanguageQuery` no `McpToolsRegistry`
- [ ] Prompt engineering:
  ```
  System: Você é um especialista em SQL. Dado o schema:
  {schema}
  
  Gere uma query SQL válida para a pergunta:
  {question}
  
  Retorne APENAS o SQL, sem explicações.
  ```
- [ ] Validação de SQL gerado (mesmo processo de `secureDatabaseQuery`)
- [ ] Refinamento iterativo (se SQL inválido, retry com erro como contexto)
- [ ] Testes E2E com perguntas reais

**Parâmetros:**
```json
{
  "name": "naturalLanguageQuery",
  "description": "Execute queries usando linguagem natural",
  "inputSchema": {
    "type": "object",
    "properties": {
      "question": {
        "type": "string",
        "description": "Pergunta em linguagem natural"
      },
      "maxRows": {
        "type": "integer",
        "default": 100
      }
    },
    "required": ["question"]
  }
}
```

**Retorno:**
```json
{
  "content": [{
    "type": "text",
    "text": "{
      \"generatedSQL\": \"SELECT ...\",
      \"results\": [...],
      \"rowCount\": 10,
      \"llmProvider\": \"openai\",
      \"llmModel\": \"gpt-4-turbo\",
      \"tokensUsed\": 1250,
      \"costUSD\": 0.0375
    }"
  }]
}
```

---

#### 4. Nova Ferramenta: `explainQuery` (2 dias)

**Objetivo:** Receber SQL e retornar explicação em linguagem natural.

**Exemplo:**
```
Input: SELECT c.name, SUM(o.total) FROM customers c JOIN orders o ON c.id = o.customer_id GROUP BY c.name ORDER BY SUM(o.total) DESC LIMIT 10

Output: "Esta consulta busca os 10 clientes que mais gastaram. 
Ela junta a tabela de clientes com a tabela de pedidos,
soma o total de pedidos de cada cliente, e retorna os 10 maiores valores."
```

**Tasks:**
- [ ] Criar ferramenta `explainQuery`
- [ ] Prompt engineering para explicações claras
- [ ] Testes com queries complexas
- [ ] Documentação

---

#### 5. Nova Ferramenta: `suggestQueryOptimizations` (2 dias)

**Objetivo:** Analisar query e sugerir melhorias.

**Tasks:**
- [ ] Integrar com `EXPLAIN` do banco
- [ ] LLM analisa o plan e sugere:
  - Índices faltantes
  - Reescrita de query
  - Hints de performance
- [ ] Testes
- [ ] Documentação

---

### 💰 Semana 4: Cost Dashboard e Analytics

#### 6. Enhanced Cost Tracking (3 dias)

**Objetivo:** Melhorar tracking de custos reais.

**Tasks:**
- [ ] Atualizar `TokenizationMetrics` para usar custos reais (não estimados)
- [ ] Breakdown por LLM provider
- [ ] Endpoint `/mcp/analytics/cost?from=X&to=Y&provider=openai`
- [ ] Comparação de custos entre providers
- [ ] Alertas de threshold (exemplo: se custo/dia > $10, avisar)

---

#### 7. Simple Web Dashboard (2 dias)

**Objetivo:** UI simples para visualizar custos e métricas.

**Tasks:**
- [ ] Criar `src/main/resources/static/dashboard.html`
- [ ] Chart.js para gráficos
- [ ] Endpoint `/mcp/dashboard` serve HTML estático
- [ ] Gráficos:
  - Custos ao longo do tempo
  - Breakdown por ferramenta
  - Breakdown por LLM provider
  - Top queries por custo
  - Cache hit rate

**Tech Stack:**
- HTML + CSS (simples, sem framework)
- Chart.js (via CDN)
- Fetch API para buscar dados de `/mcp/metrics` e `/mcp/analytics/cost`

---

#### 8. Usage Reporting (2 dias)

**Tasks:**
- [ ] Endpoint `/mcp/reports/usage?from=X&to=Y`
- [ ] Export CSV/JSON
- [ ] Relatórios automáticos (opcional: email/webhook)
- [ ] Documentação

---

## 📊 Métricas de Sucesso da v0.2.0

### Critérios de Aceitação:
- ✅ Integração com no mínimo 2 LLM providers (OpenAI + Ollama)
- ✅ Ferramenta `naturalLanguageQuery` funcional
- ✅ Text-to-SQL accuracy > 80% (testar com 20 perguntas padrão)
- ✅ Custos reais sendo trackados (não estimados)
- ✅ Dashboard web funcional
- ✅ Cobertura de testes ≥ 78%
- ✅ Todos os testes passando
- ✅ Documentação atualizada

### KPIs:
| Métrica | v0.1.0 | Meta v0.2.0 |
|---------|--------|-------------|
| Cobertura | 74% | 78% |
| Testes | 31 | ~40 |
| Ferramentas MCP | 4 | 7 |
| LLM Providers | 0 | 4 |
| Tempo médio | <100ms | <150ms* |
| Accuracy Text-to-SQL | N/A | >80% |

\* *Inclui latência de chamadas LLM*

---

## 🔄 Processo de Desenvolvimento

### Workflow Recomendado:

```bash
# 1. Criar branch feature
git checkout -b feature/openai-integration

# 2. Desenvolvimento TDD
# - Escrever testes primeiro
# - Implementar funcionalidade
# - Refatorar

# 3. Executar testes localmente
mvn clean test

# 4. Verificar cobertura
mvn jacoco:report
firefox target/site/jacoco/index.html

# 5. Commit incremental
git add .
git commit -m "feat: adicionar OpenAI provider com testes"

# 6. Push e PR
git push origin feature/openai-integration
# Criar PR no GitHub

# 7. Merge após revisão
# CI/CD executa testes automaticamente

# 8. Após merge, branch main está atualizada
git checkout main
git pull origin main
```

---

## 📚 Documentação a Criar/Atualizar

### Novos Documentos:
- [ ] `docs/OPENAI_SETUP.md` - Setup do OpenAI API
- [ ] `docs/CLAUDE_SETUP.md` - Setup do Anthropic Claude
- [ ] `docs/GEMINI_SETUP.md` - Setup do Google Gemini
- [ ] `docs/OLLAMA_SETUP.md` - Setup do Ollama local
- [ ] `docs/TEXT_TO_SQL.md` - Como usar Text-to-SQL
- [ ] `docs/LLM_COMPARISON.md` - Comparação de providers
- [ ] `docs/COST_OPTIMIZATION.md` - Como reduzir custos

### Atualizar:
- [ ] `README.md` - Adicionar seção de LLM integration
- [ ] `QUICKSTART.md` - Adicionar exemplos de Text-to-SQL
- [ ] `ROADMAP.md` - Marcar v0.2.0 como em progresso
- [ ] `IMPLEMENTATION_SUMMARY.md` - Adicionar arquitetura de LLMs

---

## 🧪 Testes Importantes

### Testes de Integração LLM:
```java
@Test
void testOpenAI_GenerateSQL_SimpleQuery() {
    String nl = "Liste os 5 clientes mais recentes";
    String sql = llmService.generateSQL(nl, schemaContext);
    
    assertTrue(sql.toUpperCase().contains("SELECT"));
    assertTrue(sql.toUpperCase().contains("LIMIT 5"));
}

@Test
void testOpenAI_TokenCounting() {
    String text = "Hello, world!";
    int tokens = openAIProvider.countTokens(text);
    
    assertTrue(tokens > 0);
    assertTrue(tokens < 10); // ~3-4 tokens esperados
}

@Test
void testCostTracking_RealAPI() {
    String nl = "Mostre todos os produtos";
    QueryResult result = naturalLanguageQueryService.execute(nl, 100);
    
    assertNotNull(result.getTokenizationMetrics());
    assertTrue(result.getTokenizationMetrics().getCostUSD() > 0);
}
```

### Teste E2E Completo:
```java
@Test
void testFullJourney_NaturalLanguageToResults() {
    // 1. Schema understanding
    String schema = schemaService.getSchemaInfo().schemaPrompt();
    
    // 2. Natural language query
    String question = "Quais os 10 produtos mais vendidos no último mês?";
    
    // 3. Generate SQL via LLM
    String sql = llmService.generateSQL(question, schema);
    
    // 4. Validate SQL
    assertTrue(sql.toUpperCase().startsWith("SELECT"));
    
    // 5. Execute query
    QueryResult result = queryService.executeQuery(sql, 10);
    
    // 6. Verify results
    assertNotNull(result);
    assertTrue(result.getRowCount() <= 10);
    
    // 7. Verify tokenization
    assertNotNull(result.getTokenizationMetrics());
    assertTrue(result.getTokenizationMetrics().getEstimatedTokens() > 0);
    assertTrue(result.getTokenizationMetrics().getCostUSD() > 0);
}
```

---

## ⚠️ Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| **Custo alto de APIs LLM** | Alto | Cache agressivo, usar Ollama para dev, rate limiting |
| **Accuracy baixa do Text-to-SQL** | Médio | Prompt engineering, refinamento iterativo, feedback loop |
| **Latência de APIs LLM** | Médio | Cache, async processing, timeout configurável |
| **Dependência de APIs externas** | Alto | Fallback para Ollama local, retry logic, circuit breaker |
| **Complexidade de integração** | Médio | Abstrair em interface, testes mocados primeiro |

---

## 💡 Dicas para Desenvolvimento

### 1. Começar com Mocks
```java
// Implementar MockLLMProvider primeiro
// Permite testar sem gastar dinheiro em APIs
public class MockLLMProvider implements LLMProvider {
    @Override
    public String generateSQL(String nl, String schema) {
        return "SELECT * FROM customers LIMIT 10"; // Resposta fixa
    }
}
```

### 2. Usar Variáveis de Ambiente
```bash
# .env (não commitado)
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...
GOOGLE_API_KEY=AI...
```

### 3. Cache Agressivo
```properties
# Cache de respostas LLM por 24h
spring.cache.cache-names=schema-info,llm-responses
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=24h
```

### 4. Monitoring de Custos
```java
// Alert quando custo diário exceder threshold
if (dailyCost > MAX_DAILY_COST) {
    log.error("Daily cost exceeded: ${}", dailyCost);
    // Enviar email/Slack/etc
}
```

---

## 📅 Checklist Final para Release v0.2.0

Antes de criar a release:

- [ ] Todos os testes passando (≥ 40 testes)
- [ ] Cobertura ≥ 78%
- [ ] Integração com no mínimo 2 LLM providers
- [ ] Ferramenta `naturalLanguageQuery` funcional
- [ ] Text-to-SQL accuracy ≥ 80%
- [ ] Dashboard web funcional
- [ ] Documentação atualizada
- [ ] Changelog atualizado
- [ ] Performance metrics dentro do esperado
- [ ] Testes E2E passando
- [ ] Build do Maven sem erros
- [ ] Criar tag `REL-0.2.0`
- [ ] Push para GitHub
- [ ] Verificar workflow CI/CD
- [ ] Publicar release notes

---

## 🎯 Depois da v0.2.0

Após completar v0.2.0, as próximas prioridades são:

1. **v0.3.0 - Segurança** (Auth, Rate Limiting, Audit)
2. **v0.4.0 - Performance** (Redis, Async, Multi-tenancy)
3. **v0.5.0 - Observabilidade** (Prometheus, Grafana)
4. **v0.6.0 - Developer Experience** (SDKs, CLI, Web UI)

Veja [ROADMAP.md](ROADMAP.md) para detalhes completos.

---

## 📞 Precisa de Ajuda?

- **Roadmap Completo**: [ROADMAP.md](ROADMAP.md)
- **Issues**: https://github.com/magacho/aiToSql/issues
- **Discussions**: https://github.com/magacho/aiToSql/discussions
- **Mantenedor**: @magacho

---

**Boa sorte com o desenvolvimento da v0.2.0! 🚀**

**Próxima revisão**: Após merge do primeiro PR de LLM integration  
**Data**: 28 de Outubro de 2024
