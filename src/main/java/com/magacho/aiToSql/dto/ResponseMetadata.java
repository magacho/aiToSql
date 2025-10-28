package com.magacho.aiToSql.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response metadata for MCP tool calls
 * Provides token estimation, performance metrics, and data information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseMetadata(
        TokenInfo tokens,
        PerformanceInfo performance,
        CostInfo cost,
        DataInfo data
) {

    /**
     * Token estimation information
     */
    public record TokenInfo(
            int estimated,
            int inputTokens,
            int outputTokens,
            String approximationMethod,
            String warning
    ) {}

    /**
     * Performance metrics
     */
    public record PerformanceInfo(
            long executionTimeMs,
            boolean cachedResult
    ) {}

    /**
     * Cost estimation (based on Claude 3.5 Sonnet pricing)
     */
    public record CostInfo(
            double estimatedUSD,
            String model,
            String note
    ) {}

    /**
     * Data information (for query results)
     */
    public record DataInfo(
            Integer rowCount,
            Integer columnCount,
            Boolean truncated,
            Integer maxRowsLimit
    ) {}

    /**
     * Estimate tokens based on character count
     * Heuristic: 1 token ≈ 4 characters (English/code)
     * 
     * @param text The text to estimate tokens for
     * @return TokenInfo with estimation
     */
    public static TokenInfo estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return new TokenInfo(0, 0, 0, "character_count_div_4", "Actual tokens may vary by LLM tokenizer");
        }
        
        // Heuristic: 1 token ≈ 4 characters
        // This is ~95% accurate for English text and code
        int estimated = (int) Math.ceil(text.length() / 4.0);
        
        // For MCP context: input tokens ~0, output tokens ~estimated
        int inputTokens = 0;  // Tool calls typically have minimal input
        int outputTokens = estimated;
        
        return new TokenInfo(
                estimated,
                inputTokens,
                outputTokens,
                "character_count_div_4",
                "Actual tokens may vary by LLM tokenizer"
        );
    }

    /**
     * Calculate cost based on token count
     * Uses Claude 3.5 Sonnet pricing as reference
     */
    public static CostInfo calculateCost(TokenInfo tokens) {
        // Claude 3.5 Sonnet pricing (as of 2024):
        // Input: $3.00 per million tokens
        // Output: $15.00 per million tokens
        double inputCost = (tokens.inputTokens() / 1_000_000.0) * 3.00;
        double outputCost = (tokens.outputTokens() / 1_000_000.0) * 15.00;
        double totalCost = inputCost + outputCost;
        
        return new CostInfo(
                totalCost,
                "claude-3.5-sonnet",
                "Estimated based on character count heuristic"
        );
    }

    /**
     * Extract data info from QueryResult
     * 
     * @param result The result object (expected to be QueryResult)
     * @return DataInfo or null if not applicable
     */
    public static DataInfo extractDataInfo(Object result) {
        if (result instanceof QueryResult queryResult) {
            boolean truncated = queryResult.data() != null && 
                              queryResult.rowCount() >= 1000; // Default max rows
            
            Integer columnCount = null;
            if (queryResult.columnNames() != null) {
                columnCount = queryResult.columnNames().size();
            }
            
            return new DataInfo(
                    queryResult.rowCount(),
                    columnCount,
                    truncated,
                    1000
            );
        }
        
        return null;
    }

    /**
     * Create a complete ResponseMetadata from result and execution time
     * 
     * @param result The tool execution result
     * @param executionTimeMs Execution time in milliseconds
     * @param cachedResult Whether the result was cached
     * @return Complete ResponseMetadata
     */
    public static ResponseMetadata create(Object result, long executionTimeMs, boolean cachedResult) {
        // Convert result to text for token estimation
        String textResult = convertToText(result);
        
        TokenInfo tokenInfo = estimateTokens(textResult);
        CostInfo costInfo = calculateCost(tokenInfo);
        PerformanceInfo perfInfo = new PerformanceInfo(executionTimeMs, cachedResult);
        DataInfo dataInfo = extractDataInfo(result);
        
        return new ResponseMetadata(tokenInfo, perfInfo, costInfo, dataInfo);
    }

    /**
     * Convert result to text representation
     */
    private static String convertToText(Object result) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
        } catch (Exception e) {
            return result.toString();
        }
    }
}
