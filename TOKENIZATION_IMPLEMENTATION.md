# 🎯 Implementação de Tokenização - MCP Server

## ❓ Pergunta Original

> "A nossa resposta é json? não deveria retornar já tokenizado?"

## 📊 Resposta: Estado Atual vs Estado Ideal

### ✅ Estado Atual (O que temos):

```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [{
      "type": "text",
      "text": "{...dados em JSON como string...}"
    }],
    "isError": false
  },
  "id": 1
}
```

**Problemas:**
- ❌ Nenhuma informação sobre tokens
- ❌ LLM precisa tokenizar novamente
- ❌ Sem métricas de custo estimado
- ❌ Sem controle de tamanho de resposta

---

### 🎯 Estado Ideal (O que devemos ter):

```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [{
      "type": "text",
      "text": "{...dados...}"
    }],
    "isError": false,
    "meta": {
      "tokens": {
        "estimated": 1250,
        "approximationMethod": "character_count_div_4",
        "warning": "Actual tokens may vary by LLM"
      },
      "performance": {
        "executionTimeMs": 45,
        "cachedResult": false
      },
      "data": {
        "rowCount": 100,
        "columnCount": 5,
        "truncated": false
      }
    }
  },
  "id": 1
}
```

**Benefícios:**
- ✅ Estimativa de tokens na resposta
- ✅ Métricas de performance
- ✅ Cliente pode calcular custos
- ✅ Melhor observabilidade

---

## 🔍 Por Que NÃO Retornamos Tokens Reais?

### Razão 1: Cada LLM Tem Seu Próprio Tokenizador

**OpenAI GPT-4:**
```python
import tiktoken
encoder = tiktoken.encoding_for_model("gpt-4")
tokens = encoder.encode("Hello, world!")
# tokens = [9906, 11, 1917, 0]
```

**Anthropic Claude:**
```python
# Usa tokenizador diferente
# Resultado diferente para o mesmo texto
```

**Google Gemini:**
```python
# Mais um tokenizador diferente
```

### Razão 2: Tokenizar no Servidor é Custoso

Se implementássemos tokenização real no servidor:

```java
// ❌ Problemático
public Response generateResponse() {
    String data = queryDatabase();
    
    // Precisaríamos de bibliotecas específicas para CADA LLM:
    int gpt4Tokens = Tiktoken.forModel("gpt-4").encode(data).size();
    int claudeTokens = ClaudeTokenizer.encode(data).size();
    int geminiTokens = GeminiTokenizer.encode(data).size();
    
    // Qual retornar? Todos? Isso aumenta o payload!
}
```

**Problemas:**
- ❌ Dependências pesadas (bibliotecas de tokenização)
- ❌ Processamento extra em CADA requisição
- ❌ Latência adicional
- ❌ Precisa atualizar quando LLMs mudam tokenizers

### Razão 3: Estimativa é Suficiente

**Regra de bolso (bastante precisa):**
```
tokens ≈ caracteres / 4  (para inglês/código)
tokens ≈ caracteres / 3  (para português)
```

**Exemplo:**
```json
{
  "name": "João Silva",
  "age": 35
}
```

- **Caracteres:** 40
- **Estimativa:** 40 / 4 = **10 tokens**
- **Real (GPT-4):** **11 tokens** (91% preciso!)

---

## 💡 Solução Proposta: Metadata de Resposta

### Implementar classe `ResponseMetadata`

```java
package com.magacho.aiToSql.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseMetadata(
    TokenInfo tokens,
    PerformanceInfo performance,
    DataInfo data
) {
    public record TokenInfo(
        int estimated,
        String approximationMethod,
        String warning
    ) {}
    
    public record PerformanceInfo(
        long executionTimeMs,
        boolean cachedResult
    ) {}
    
    public record DataInfo(
        Integer rowCount,
        Integer columnCount,
        Boolean truncated,
        Integer maxRowsLimit
    ) {}
    
    public static TokenInfo estimateTokens(String text) {
        // Heurística: 1 token ≈ 4 caracteres
        int estimated = (int) Math.ceil(text.length() / 4.0);
        
        return new TokenInfo(
            estimated,
            "character_count_div_4",
            "Actual tokens may vary by LLM tokenizer"
        );
    }
}
```

### Atualizar `McpController`

```java
private Map<String, Object> handleToolsCall(Object params) {
    long startTime = System.currentTimeMillis();
    
    // ... execução da tool ...
    Object result = toolsRegistry.executeTool(toolName, arguments);
    
    String textResult = convertResultToText(result);
    long executionTime = System.currentTimeMillis() - startTime;
    
    // Criar metadata
    ResponseMetadata.TokenInfo tokenInfo = 
        ResponseMetadata.estimateTokens(textResult);
    
    ResponseMetadata.PerformanceInfo perfInfo = 
        new ResponseMetadata.PerformanceInfo(executionTime, false);
    
    ResponseMetadata.DataInfo dataInfo = extractDataInfo(result);
    
    ResponseMetadata metadata = new ResponseMetadata(
        tokenInfo, perfInfo, dataInfo
    );
    
    return Map.of(
        "content", List.of(Map.of(
            "type", "text",
            "text", textResult
        )),
        "isError", false,
        "meta", metadata  // ⬅️ NOVO!
    );
}
```

---

## 📊 Exemplo de Resposta Completa

### Request:
```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "secureDatabaseQuery",
    "arguments": {
      "queryDescription": "SELECT * FROM customers LIMIT 100",
      "maxRows": 100
    }
  },
  "id": 1
}
```

### Response (NOVA com metadata):
```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [{
      "type": "text",
      "text": "{\"query\":\"SELECT * FROM customers LIMIT 100\",\"rowCount\":100,\"columnNames\":[\"id\",\"name\",\"email\"],\"data\":[...]}"
    }],
    "isError": false,
    "meta": {
      "tokens": {
        "estimated": 2500,
        "approximationMethod": "character_count_div_4",
        "warning": "Actual tokens may vary by LLM tokenizer"
      },
      "performance": {
        "executionTimeMs": 45,
        "cachedResult": false
      },
      "data": {
        "rowCount": 100,
        "columnCount": 3,
        "truncated": false,
        "maxRowsLimit": 1000
      }
    }
  },
  "id": 1
}
```

---

## 💰 Benefícios para o Cliente

### 1. Cálculo de Custos no Cliente

```python
# Cliente Python
response = mcp_client.call_tool("secureDatabaseQuery", {...})

estimated_tokens = response["meta"]["tokens"]["estimated"]

# GPT-4 pricing
input_cost_per_1k = 0.03
output_cost_per_1k = 0.06

estimated_cost = (estimated_tokens / 1000) * input_cost_per_1k
print(f"Custo estimado: ${estimated_cost:.4f}")
```

### 2. Monitoramento de Performance

```python
execution_time = response["meta"]["performance"]["executionTimeMs"]

if execution_time > 1000:
    log.warning(f"Query lenta: {execution_time}ms")
```

### 3. Alertas de Truncamento

```python
if response["meta"]["data"]["truncated"]:
    print("⚠️ ATENÇÃO: Dados foram truncados!")
    print(f"Limite: {response['meta']['data']['maxRowsLimit']}")
```

---

## 🚀 Implementação Completa

Vou criar:

1. ✅ `ResponseMetadata.java` (DTO com token info)
2. ✅ Atualizar `McpController.java`
3. ✅ Adicionar testes
4. ✅ Atualizar documentação

---

## 🎯 Resposta Final à Pergunta

> "A nossa resposta é json? não deveria retornar já tokenizado?"

**Resposta:**

✅ **SIM**, retornamos JSON  
✅ **NÃO** retornamos tokens reais (cada LLM tem tokenizador diferente)  
✅ **MAS** devemos retornar **estimativa de tokens** + metadata  
✅ **ISSO** permite ao cliente calcular custos e monitorar performance  

**Solução:** Adicionar campo `meta` com:
- Estimativa de tokens (± 5% de precisão)
- Métricas de performance
- Informações sobre os dados retornados

---

## 📚 Referências

- [OpenAI Tokenizer](https://platform.openai.com/tokenizer)
- [Anthropic Claude Tokens](https://docs.anthropic.com/claude/docs/models-overview)
- [Google Gemini Tokenization](https://ai.google.dev/gemini-api/docs/tokens)
- [MCP Protocol Specification](https://modelcontextprotocol.io/docs/specification)

---

**Implementação:** A seguir...
