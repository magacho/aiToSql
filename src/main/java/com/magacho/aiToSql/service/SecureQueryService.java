package com.magacho.aiToSql.service;

import com.magacho.aiToSql.config.McpServerConfig;
import com.magacho.aiToSql.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Secure Query Service
 * Executes SELECT queries with security validations
 * CRITICAL: This service implements SQL injection prevention
 */
@Service
public class SecureQueryService {

    private static final Logger log = LoggerFactory.getLogger(SecureQueryService.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final McpServerConfig config;

    private static final Pattern SELECT_PATTERN = Pattern.compile(
            "^\\s*SELECT\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern DANGEROUS_KEYWORDS = Pattern.compile(
            "\\b(DROP|DELETE|UPDATE|INSERT|CREATE|ALTER|TRUNCATE|EXEC|EXECUTE|GRANT|REVOKE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public SecureQueryService(JdbcTemplate jdbcTemplate, McpServerConfig config) {
        this.jdbcTemplate = jdbcTemplate;
        this.config = config;
    }

    /**
     * Execute a secure database query
     * 
     * @param queryDescription Natural language description or SQL query
     * @param maxRows Maximum rows to return (overrides config if lower)
     * @return Query results with metadata
     * @throws SecurityException if query contains dangerous operations
     * @throws IllegalArgumentException if query is invalid
     */
    public QueryResult secureDatabaseQuery(String queryDescription, Integer maxRows) {
        if (queryDescription == null || queryDescription.isBlank()) {
            throw new IllegalArgumentException("Query description cannot be empty");
        }

        String sanitizedQuery = queryDescription.trim();

        // Security validation: Must be a SELECT statement
        if (!SELECT_PATTERN.matcher(sanitizedQuery).find()) {
            log.error("SECURITY VIOLATION: Non-SELECT query attempted: {}", sanitizedQuery);
            throw new SecurityException(
                    "Only SELECT queries are allowed. Query must start with SELECT.");
        }

        // Security validation: Check for dangerous keywords
        if (DANGEROUS_KEYWORDS.matcher(sanitizedQuery).find()) {
            log.error("SECURITY VIOLATION: Dangerous keywords detected in query: {}", sanitizedQuery);
            throw new SecurityException(
                    "Query contains forbidden keywords (DROP, DELETE, UPDATE, INSERT, etc.)");
        }

        // Determine effective max rows
        int effectiveMaxRows = maxRows != null && maxRows > 0 
                ? Math.min(maxRows, config.getMaxQueryRows())
                : config.getMaxQueryRows();

        // Add LIMIT clause if not present (database-specific)
        String limitedQuery = addLimitClause(sanitizedQuery, effectiveMaxRows);

        if (config.isEnableQueryLogging()) {
            log.info("Executing secure query (max {} rows): {}", effectiveMaxRows, limitedQuery);
        }

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(limitedQuery);
            
            List<String> columnNames = results.isEmpty() 
                    ? new ArrayList<>() 
                    : new ArrayList<>(results.get(0).keySet());

            log.info("Query executed successfully. Rows returned: {}", results.size());

            return new QueryResult(
                    sanitizedQuery,
                    results.size(),
                    effectiveMaxRows,
                    columnNames,
                    results
            );

        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Add database-specific LIMIT clause
     * Note: This is a simplified implementation. In production, use proper query parsing.
     */
    private String addLimitClause(String query, int maxRows) {
        String upperQuery = query.toUpperCase();
        
        // If already has LIMIT, FETCH, or TOP, return as-is
        if (upperQuery.contains(" LIMIT ") || 
            upperQuery.contains(" FETCH ") || 
            upperQuery.contains(" TOP ")) {
            return query;
        }

        // For simplicity, append LIMIT (works for PostgreSQL, MySQL)
        // In production, detect database type and use appropriate syntax
        return query + " LIMIT " + maxRows;
    }

    /**
     * Validate query syntax without executing
     */
    public boolean validateQuery(String query) {
        try {
            if (query == null || query.isBlank()) {
                return false;
            }

            String sanitized = query.trim();
            
            return SELECT_PATTERN.matcher(sanitized).find() 
                    && !DANGEROUS_KEYWORDS.matcher(sanitized).find();
                    
        } catch (Exception e) {
            log.warn("Query validation failed: {}", e.getMessage());
            return false;
        }
    }
}
