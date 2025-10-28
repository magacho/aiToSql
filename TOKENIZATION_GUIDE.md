# Tokenização de Respostas para LLMs - MCP Server

## O que é Tokenização?

**Tokenização** é o processo de converter texto em "tokens" que os LLMs (Large Language Models) podem processar. Cada LLM tem seu próprio tokenizador, mas geralmente:

- 1 token ≈ 4 caracteres em português
- 1 token ≈ 0.75 palavras em inglês
- Números, símbolos e código são tokenizados de forma diferente

## Por que isso é importante no MCP Server?

Quando o LLM chama as ferramentas do MCP Server, as respostas precisam ser:

1. **Estruturadas** - Formato JSON consistente
2. **Concisas** - Minimizar tokens para economizar custos
3. **Informativas** - Dados essenciais para o LLM gerar insights
4. **Parseáveis** - Fácil de extrair informações específicas

## Como o MCP Server Otimiza Respostas

### 1. Formato JSON Estruturado

```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [{
      "type": "text",
      "text": "{...dados estruturados...}"
    }],
    "isError": false
  },
  "id": 1
}
```

**Vantagens:**
- ✅ LLM identifica facilmente o formato
- ✅ Extração de dados via JSON parsing
- ✅ Previsibilidade para fine-tuning

### 2. Compactação de Metadados

**Exemplo: Schema Structure**

❌ **Formato Verbose (NÃO usado):**
```json
{
  "database_name": "production",
  "database_type": "PostgreSQL",
  "tables": [
    {
      "table_name": "customers",
      "table_type": "TABLE",
      "columns": [
        {
          "column_name": "id",
          "data_type": "BIGINT",
          "column_size": 19,
          "is_nullable": false,
          "is_primary_key": true
        }
      ]
    }
  ]
}
```

✅ **Formato Compacto (usado):**
```json
{
  "databaseName": "production",
  "databaseType": "PostgreSQL",
  "tables": [
    {
      "tableName": "customers",
      "tableType": "TABLE",
      "columns": [
        {
          "columnName": "id",
          "dataType": "BIGINT",
          "columnSize": 19,
          "nullable": false,
          "isPrimaryKey": true
        }
      ]
    }
  ]
}
```

**Economia:** ~15% menos tokens usando camelCase e nomes curtos

### 3. Paginação e Limites

O MCP Server limita automaticamente os resultados:

```java
// Configurável em application.properties
mcp.max-query-rows=1000
```

**Por quê?**
- ✅ Previne respostas gigantescas (ex: 100k linhas = milhões de tokens)
- ✅ Reduz custos de API do LLM
- ✅ Melhora tempo de resposta

### 4. Conversão para Texto Legível

O MCP Server converte JSON para texto formatado para o LLM:

```java
private String convertResultToText(Object result) {
    return new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(result);
}
```

**Resultado:**
```
{
  "databaseName" : "production",
  "databaseType" : "PostgreSQL",
  "tables" : [ {
    "tableName" : "customers",
    ...
  } ]
}
```

## Estimativa de Tokens por Ferramenta

### Tool 1: getSchemaStructure

**Entrada (Request):**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "getSchemaStructure",
    "arguments": {}
  },
  "id": 1
}
```
**Tokens:** ~40 tokens

**Saída (Response) para 10 tabelas:**
```
Estrutura básica: ~100 tokens
+ 10 tabelas × ~50 tokens/tabela = 500 tokens
+ 5 colunas/tabela × 10 tabelas × ~10 tokens/coluna = 500 tokens
```
**Total:** ~1,100 tokens

### Tool 2: getTableDetails

**Entrada:** ~50 tokens  
**Saída (1 tabela com 20 colunas, 5 índices, 3 FKs):**
```
Cabeçalho: ~50 tokens
Colunas: 20 × ~30 tokens = 600 tokens
Índices: 5 × ~20 tokens = 100 tokens
Foreign Keys: 3 × ~30 tokens = 90 tokens
```
**Total:** ~840 tokens

### Tool 3: listTriggers

**Entrada:** ~50 tokens  
**Saída (3 triggers):**
```
Cabeçalho: ~30 tokens
Triggers: 3 × ~100 tokens = 300 tokens
```
**Total:** ~330 tokens

### Tool 4: secureDatabaseQuery

**Entrada (com SQL):** ~100 tokens  
**Saída (100 linhas, 5 colunas):**
```
Metadados: ~50 tokens
Dados: 100 linhas × 5 colunas × ~5 tokens = 2,500 tokens
```
**Total:** ~2,550 tokens

## Estratégias de Otimização de Tokens

### 1. Usar Abreviações Consistentes

```json
// Em vez de:
{
  "is_primary_key": true,
  "is_nullable": false,
  "is_auto_increment": true
}

// Usar:
{
  "isPrimaryKey": true,
  "nullable": false,
  "isAutoIncrement": true
}
```

### 2. Remover Dados Redundantes

```json
// NÃO incluir timestamp se não necessário
{
  "query": "SELECT * FROM customers",
  "executedAt": "2024-01-15T10:30:00Z",  // ❌ Desnecessário
  "rowCount": 10,
  "data": [...]
}

// Incluir apenas dados essenciais
{
  "query": "SELECT * FROM customers",
  "rowCount": 10,
  "data": [...]
}
```

### 3. Usar Tipos de Dados Apropriados

```json
// Em vez de strings para números:
{
  "age": "35",        // ❌ String = mais tokens
  "price": "1500.00"  // ❌ String = mais tokens
}

// Usar números nativos:
{
  "age": 35,       // ✅ Number = menos tokens
  "price": 1500.0  // ✅ Number = menos tokens
}
```

### 4. Limitar Profundidade de Nested Objects

```json
// Evitar:
{
  "customer": {
    "address": {
      "street": {
        "name": "Main St",
        "number": {
          "value": 123
        }
      }
    }
  }
}

// Preferir:
{
  "customer": {
    "addressStreet": "Main St",
    "addressNumber": 123
  }
}
```

## Como os LLMs Processam as Respostas

### Fluxo de Tokenização

```
1. LLM faz chamada MCP → 
2. MCP Server executa ferramenta → 
3. Resultado em JSON → 
4. Conversão para texto formatado → 
5. LLM tokeniza a resposta → 
6. LLM processa tokens → 
7. LLM gera resposta para usuário
```

### Exemplo Completo

**Prompt do Usuário:**
```
"Quantos clientes temos no Brasil?"
```

**LLM → MCP Server:**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "secureDatabaseQuery",
    "arguments": {
      "queryDescription": "SELECT COUNT(*) as total FROM customers WHERE country = 'Brazil'",
      "maxRows": 10
    }
  },
  "id": 1
}
```
**Tokens:** ~80 tokens

**MCP Server → LLM:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [{
      "type": "text",
      "text": "{\"query\":\"SELECT COUNT(*) as total FROM customers WHERE country = 'Brazil'\",\"rowCount\":1,\"columnNames\":[\"total\"],\"data\":[{\"total\":125}]}"
    }],
    "isError": false
  },
  "id": 1
}
```
**Tokens:** ~120 tokens

**LLM → Usuário:**
```
"Atualmente temos 125 clientes cadastrados no Brasil."
```
**Tokens:** ~15 tokens

**Total de tokens consumidos:** 80 + 120 + 15 = **215 tokens**

## Custos Aproximados

Usando **GPT-4** como exemplo:
- Input: $0.03 / 1K tokens
- Output: $0.06 / 1K tokens

**Custo por consulta:**
```
Input:  200 tokens × $0.03 / 1000 = $0.006
Output: 15 tokens × $0.06 / 1000  = $0.0009
Total:                              $0.0069 (~$0.007)
```

**Para 1.000 consultas/dia:**
```
1.000 × $0.007 = $7.00/dia
30 dias = $210.00/mês
```

## Otimizações Implementadas no MCP Server

| Otimização | Economia de Tokens | Status |
|------------|-------------------|--------|
| **Limitar resultados** | 50-90% | ✅ Implementado |
| **JSON compacto** | 15-20% | ✅ Implementado |
| **Caching de schema** | 100% em hits | ✅ Implementado |
| **Tipos nativos** | 5-10% | ✅ Implementado |
| **CamelCase vs snake_case** | 10-15% | ✅ Implementado |

## Boas Práticas para Desenvolvedores

### 1. Sempre Limitar Resultados

```java
// ✅ BOM
public QueryResult secureDatabaseQuery(String query, Integer maxRows) {
    int effectiveMaxRows = Math.min(maxRows != null ? maxRows : 1000, 1000);
    // ...
}

// ❌ RUIM (sem limite)
public QueryResult secureDatabaseQuery(String query) {
    return jdbcTemplate.queryForList(query);
}
```

### 2. Usar Records (Java) ou DTOs Simples

```java
// ✅ BOM - Record compacto
public record Customer(Long id, String name, String email) {}

// ❌ RUIM - Classe com muitos getters/setters verbose
public class Customer {
    private Long customerId;
    private String customerName;
    private String customerEmailAddress;
    // 20 linhas de getters/setters...
}
```

### 3. Paginar Grandes Resultados

```java
// ✅ BOM
public QueryResult getCustomers(int page, int pageSize) {
    int offset = page * pageSize;
    String sql = "SELECT * FROM customers LIMIT ? OFFSET ?";
    // ...
}
```

### 4. Cache Agressivo

```java
// ✅ BOM - Cache de 30 minutos
@Cacheable(value = "schema-structure", key = "#databaseName")
public SchemaStructure getSchemaStructure(String databaseName) {
    // Operação cara
}
```

## Monitoramento de Tokens

### Logging de Tamanho de Resposta

```java
log.info("Schema structure retrieved: {} characters (~{} tokens)", 
    schemaPrompt.length(), 
    schemaPrompt.length() / 4);
```

### Métricas Recomendadas

1. **Tamanho médio de resposta** (caracteres)
2. **Tempo de resposta**
3. **Taxa de cache hit**
4. **Número de tokens por ferramenta**

## Conclusão

O MCP Server foi projetado para **minimizar custos de tokens** enquanto fornece **informações completas e estruturadas** para LLMs. As otimizações incluem:

✅ Respostas JSON compactas  
✅ Limites configuráveis de resultados  
✅ Caching agressivo de metadados  
✅ Tipos de dados nativos  
✅ Nomenclatura eficiente  

Com essas otimizações, o custo médio por consulta é de **$0.007** usando GPT-4, tornando viável o uso em produção com milhares de consultas diárias.
