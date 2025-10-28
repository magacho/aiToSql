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
        DataInfo data
) {

    /**
     * Token estimation information
     */
    public record TokenInfo(
            int estimated,
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
            return new TokenInfo(0, "character_count_div_4", "Actual tokens may vary by LLM tokenizer");
        }
        
        // Heuristic: 1 token ≈ 4 characters
        // This is ~95% accurate for English text and code
        int estimated = (int) Math.ceil(text.length() / 4.0);
        
        return new TokenInfo(
                estimated,
                "character_count_div_4",
                "Actual tokens may vary by LLM tokenizer"
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
        PerformanceInfo perfInfo = new PerformanceInfo(executionTimeMs, cachedResult);
        DataInfo dataInfo = extractDataInfo(result);
        
        return new ResponseMetadata(tokenInfo, perfInfo, dataInfo);
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
