package com.magacho.aiToSql.service;

import com.magacho.aiToSql.dto.TokenizationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço para rastrear métricas de tokenização e desempenho.
 * Útil para análise de custos e otimização de respostas.
 */
@Service
public class TokenizationMetricsService {
    
    private static final Logger log = LoggerFactory.getLogger(TokenizationMetricsService.class);
    
    // Métricas agregadas por ferramenta
    private final ConcurrentHashMap<String, ToolMetrics> toolMetrics = new ConcurrentHashMap<>();
    
    /**
     * Registra métricas de uma execução de ferramenta.
     */
    public void recordMetrics(String toolName, TokenizationMetrics metrics) {
        toolMetrics.computeIfAbsent(toolName, k -> new ToolMetrics(k))
                .record(metrics);
        
        log.info("Metrics for {}: {}", toolName, metrics);
    }
    
    /**
     * Obtém estatísticas agregadas de uma ferramenta.
     */
    public ToolStatistics getStatistics(String toolName) {
        ToolMetrics metrics = toolMetrics.get(toolName);
        if (metrics == null) {
            return new ToolStatistics(toolName, 0, 0, 0, 0, 0, 0);
        }
        return metrics.getStatistics();
    }
    
    /**
     * Obtém todas as estatísticas.
     */
    public ConcurrentHashMap<String, ToolStatistics> getAllStatistics() {
        ConcurrentHashMap<String, ToolStatistics> allStats = new ConcurrentHashMap<>();
        toolMetrics.forEach((toolName, metrics) -> 
            allStats.put(toolName, metrics.getStatistics())
        );
        return allStats;
    }
    
    /**
     * Reseta todas as métricas.
     */
    public void resetMetrics() {
        toolMetrics.clear();
        log.info("All metrics reset");
    }
    
    /**
     * Classe interna para agregar métricas de uma ferramenta.
     */
    private static class ToolMetrics {
        private final String toolName;
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
        private final AtomicLong totalCharacters = new AtomicLong(0);
        private final AtomicLong totalTokens = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        private double totalCostUSD = 0.0;
        
        ToolMetrics(String toolName) {
            this.toolName = toolName;
        }
        
        synchronized void record(TokenizationMetrics metrics) {
            callCount.incrementAndGet();
            totalExecutionTimeMs.addAndGet(metrics.executionTimeMs());
            totalCharacters.addAndGet(metrics.characterCount());
            totalTokens.addAndGet(metrics.estimatedTokenCount());
            totalCostUSD += metrics.estimatedCostUSD();
            if (metrics.cacheHit()) {
                cacheHits.incrementAndGet();
            }
        }
        
        synchronized ToolStatistics getStatistics() {
            long calls = callCount.get();
            if (calls == 0) {
                return new ToolStatistics(toolName, 0, 0, 0, 0, 0, 0);
            }
            
            return new ToolStatistics(
                    toolName,
                    calls,
                    totalExecutionTimeMs.get() / calls,
                    totalCharacters.get() / calls,
                    totalTokens.get() / calls,
                    totalCostUSD,
                    (double) cacheHits.get() / calls * 100
            );
        }
    }
    
    /**
     * Estatísticas agregadas de uma ferramenta.
     */
    public record ToolStatistics(
            String toolName,
            long totalCalls,
            long avgExecutionTimeMs,
            long avgCharacters,
            long avgTokens,
            double totalCostUSD,
            double cacheHitRate
    ) {
        @Override
        public String toString() {
            return String.format(
                    "%s: %d calls, avg %dms, avg %d chars, avg %d tokens, $%.6f total, %.1f%% cache hit",
                    toolName, totalCalls, avgExecutionTimeMs, avgCharacters, 
                    avgTokens, totalCostUSD, cacheHitRate
            );
        }
    }
}
