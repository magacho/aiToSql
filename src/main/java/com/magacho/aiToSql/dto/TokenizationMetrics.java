package com.magacho.aiToSql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Métricas de desempenho e tokenização para respostas MCP.
 * Permite medir custos computacionais e estimar custos de API de LLM.
 */
public record TokenizationMetrics(
        @JsonProperty("executionTimeMs")
        long executionTimeMs,
        
        @JsonProperty("characterCount")
        int characterCount,
        
        @JsonProperty("estimatedTokenCount")
        int estimatedTokenCount,
        
        @JsonProperty("estimatedCostUSD")
        double estimatedCostUSD,
        
        @JsonProperty("cacheHit")
        boolean cacheHit,
        
        @JsonProperty("timestamp")
        Instant timestamp
) {
    /**
     * Cria métricas com cálculos automáticos baseados no conteúdo.
     * 
     * @param content Conteúdo da resposta
     * @param executionTimeMs Tempo de execução em milissegundos
     * @param cacheHit Se foi cache hit
     * @return Métricas calculadas
     */
    public static TokenizationMetrics fromContent(String content, long executionTimeMs, boolean cacheHit) {
        int charCount = content.length();
        
        // Estimativa: 1 token ≈ 4 caracteres para português
        int estimatedTokens = charCount / 4;
        
        // Custo estimado usando GPT-4 pricing
        // Input: $0.03 / 1K tokens
        // Output: $0.06 / 1K tokens (assumindo 20% de output)
        double inputCost = (estimatedTokens * 0.03) / 1000.0;
        double outputCost = ((estimatedTokens * 0.2) * 0.06) / 1000.0;
        double totalCost = inputCost + outputCost;
        
        return new TokenizationMetrics(
                executionTimeMs,
                charCount,
                estimatedTokens,
                totalCost,
                cacheHit,
                Instant.now()
        );
    }
    
    /**
     * Retorna uma representação legível das métricas.
     */
    @Override
    public String toString() {
        return String.format(
                "TokenizationMetrics[time=%dms, chars=%d, tokens≈%d, cost≈$%.6f, cache=%s]",
                executionTimeMs,
                characterCount,
                estimatedTokenCount,
                estimatedCostUSD,
                cacheHit ? "HIT" : "MISS"
        );
    }
}
