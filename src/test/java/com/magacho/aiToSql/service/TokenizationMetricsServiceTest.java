package com.magacho.aiToSql.service;

import com.magacho.aiToSql.dto.TokenizationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenizationMetricsServiceTest {

    private TokenizationMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new TokenizationMetricsService();
    }

    @Test
    void testRecordMetrics() {
        // Given
        String toolName = "getSchemaStructure";
        String content = "Sample response content for testing tokenization";
        TokenizationMetrics metrics = TokenizationMetrics.fromContent(content, 150, false);

        // When
        metricsService.recordMetrics(toolName, metrics);

        // Then
        var stats = metricsService.getStatistics(toolName);
        assertNotNull(stats);
        assertEquals(1, stats.totalCalls());
        assertEquals(150, stats.avgExecutionTimeMs());
        assertTrue(stats.avgTokens() > 0);
    }

    @Test
    void testMultipleCallsAggregation() {
        // Given
        String toolName = "secureDatabaseQuery";
        
        // When - Record multiple calls
        for (int i = 0; i < 5; i++) {
            String content = "Response " + i + " with varying content length for testing purposes";
            TokenizationMetrics metrics = TokenizationMetrics.fromContent(content, 100 + i * 10, false);
            metricsService.recordMetrics(toolName, metrics);
        }

        // Then
        var stats = metricsService.getStatistics(toolName);
        assertEquals(5, stats.totalCalls());
        assertEquals(120, stats.avgExecutionTimeMs()); // (100+110+120+130+140)/5
        assertTrue(stats.totalCostUSD() > 0);
    }

    @Test
    void testCacheHitRate() {
        // Given
        String toolName = "getSchemaStructure";
        
        // When - Record 3 cache misses and 7 cache hits
        for (int i = 0; i < 10; i++) {
            boolean cacheHit = i >= 3;
            TokenizationMetrics metrics = TokenizationMetrics.fromContent("content", 50, cacheHit);
            metricsService.recordMetrics(toolName, metrics);
        }

        // Then
        var stats = metricsService.getStatistics(toolName);
        assertEquals(10, stats.totalCalls());
        assertEquals(70.0, stats.cacheHitRate(), 0.1); // 7/10 = 70%
    }

    @Test
    void testGetAllStatistics() {
        // Given
        String tool1 = "getSchemaStructure";
        String tool2 = "secureDatabaseQuery";
        
        metricsService.recordMetrics(tool1, TokenizationMetrics.fromContent("content1", 100, false));
        metricsService.recordMetrics(tool2, TokenizationMetrics.fromContent("content2", 200, false));
        metricsService.recordMetrics(tool1, TokenizationMetrics.fromContent("content3", 150, true));

        // When
        var allStats = metricsService.getAllStatistics();

        // Then
        assertEquals(2, allStats.size());
        assertTrue(allStats.containsKey(tool1));
        assertTrue(allStats.containsKey(tool2));
        assertEquals(2, allStats.get(tool1).totalCalls());
        assertEquals(1, allStats.get(tool2).totalCalls());
    }

    @Test
    void testResetMetrics() {
        // Given
        metricsService.recordMetrics("tool1", TokenizationMetrics.fromContent("content", 100, false));
        metricsService.recordMetrics("tool2", TokenizationMetrics.fromContent("content", 200, false));

        // When
        metricsService.resetMetrics();

        // Then
        var allStats = metricsService.getAllStatistics();
        assertTrue(allStats.isEmpty());
    }

    @Test
    void testStatisticsForNonExistentTool() {
        // When
        var stats = metricsService.getStatistics("nonExistentTool");

        // Then
        assertNotNull(stats);
        assertEquals(0, stats.totalCalls());
        assertEquals(0, stats.avgExecutionTimeMs());
        assertEquals(0, stats.totalCostUSD());
    }

    @Test
    void testTokenEstimation() {
        // Given - Content with ~100 characters
        String content = "This is a sample text for token estimation. It should have approximately one hundred characters here.";
        
        // When
        TokenizationMetrics metrics = TokenizationMetrics.fromContent(content, 100, false);

        // Then
        assertEquals(content.length(), metrics.characterCount());
        // 1 token ≈ 4 characters
        int expectedTokens = content.length() / 4;
        assertEquals(expectedTokens, metrics.estimatedTokenCount());
        assertTrue(metrics.estimatedCostUSD() > 0);
    }

    @Test
    void testCostEstimation() {
        // Given - Content with 4000 characters = ~1000 tokens
        String content = "x".repeat(4000);
        
        // When
        TokenizationMetrics metrics = TokenizationMetrics.fromContent(content, 100, false);

        // Then
        assertEquals(1000, metrics.estimatedTokenCount());
        // Cost for 1000 tokens input + 200 tokens output (20%)
        // Input: 1000 * 0.03 / 1000 = 0.03
        // Output: 200 * 0.06 / 1000 = 0.012
        // Total ≈ 0.042
        assertTrue(metrics.estimatedCostUSD() > 0.04 && metrics.estimatedCostUSD() < 0.05);
    }

    @Test
    void testMetricsToString() {
        // Given
        TokenizationMetrics metrics = TokenizationMetrics.fromContent("test content", 150, true);

        // When
        String str = metrics.toString();

        // Then
        assertTrue(str.contains("time=150ms"));
        assertTrue(str.contains("cache=HIT"));
        assertTrue(str.contains("tokens"));
        assertTrue(str.contains("cost"));
    }

    @Test
    void testStatisticsToString() {
        // Given
        String toolName = "testTool";
        metricsService.recordMetrics(toolName, TokenizationMetrics.fromContent("content", 100, false));
        
        // When
        var stats = metricsService.getStatistics(toolName);
        String str = stats.toString();

        // Then
        assertTrue(str.contains(toolName));
        assertTrue(str.contains("1 calls"));
        assertTrue(str.contains("100ms"));
    }
}
