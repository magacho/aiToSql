package com.magacho.aiToSql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatic JDBC Driver Resolver
 * 
 * This component automatically determines the appropriate JDBC driver class
 * based on the database type or JDBC URL pattern, eliminating the need for
 * users to manually specify the driver class name.
 * 
 * Supported databases:
 * - PostgreSQL
 * - MySQL
 * - Microsoft SQL Server (MSSQL)
 * - Oracle
 */
@Component
public class JdbcDriverResolver {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDriverResolver.class);

    private static final Map<String, String> DB_TYPE_TO_DRIVER = new HashMap<>();
    private static final Map<Pattern, String> URL_PATTERN_TO_DRIVER = new HashMap<>();

    static {
        // Database type to driver class mapping
        DB_TYPE_TO_DRIVER.put("postgresql", "org.postgresql.Driver");
        DB_TYPE_TO_DRIVER.put("mysql", "com.mysql.cj.jdbc.Driver");
        DB_TYPE_TO_DRIVER.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DB_TYPE_TO_DRIVER.put("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DB_TYPE_TO_DRIVER.put("oracle", "oracle.jdbc.OracleDriver");
        DB_TYPE_TO_DRIVER.put("h2", "org.h2.Driver");

        // JDBC URL pattern to driver class mapping
        URL_PATTERN_TO_DRIVER.put(
            Pattern.compile("^jdbc:postgresql:", Pattern.CASE_INSENSITIVE),
            "org.postgresql.Driver"
        );
        URL_PATTERN_TO_DRIVER.put(
            Pattern.compile("^jdbc:mysql:", Pattern.CASE_INSENSITIVE),
            "com.mysql.cj.jdbc.Driver"
        );
        URL_PATTERN_TO_DRIVER.put(
            Pattern.compile("^jdbc:sqlserver:", Pattern.CASE_INSENSITIVE),
            "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        );
        URL_PATTERN_TO_DRIVER.put(
            Pattern.compile("^jdbc:oracle:", Pattern.CASE_INSENSITIVE),
            "oracle.jdbc.OracleDriver"
        );
        URL_PATTERN_TO_DRIVER.put(
            Pattern.compile("^jdbc:h2:", Pattern.CASE_INSENSITIVE),
            "org.h2.Driver"
        );
    }

    /**
     * Resolves the JDBC driver class name based on database type
     * 
     * @param dbType Database type (e.g., "postgresql", "mysql", "oracle", "sqlserver")
     * @return Optional containing the driver class name, or empty if type is unknown
     */
    public Optional<String> resolveDriverByType(String dbType) {
        if (dbType == null || dbType.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedType = dbType.trim().toLowerCase();
        String driverClass = DB_TYPE_TO_DRIVER.get(normalizedType);

        if (driverClass != null) {
            logger.info("✅ Resolved driver for type '{}': {}", dbType, driverClass);
        } else {
            logger.warn("⚠️  Unknown database type: {}", dbType);
        }

        return Optional.ofNullable(driverClass);
    }

    /**
     * Resolves the JDBC driver class name based on JDBC URL pattern
     * 
     * @param jdbcUrl JDBC connection URL
     * @return Optional containing the driver class name, or empty if pattern is not recognized
     */
    public Optional<String> resolveDriverByUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            return Optional.empty();
        }

        for (Map.Entry<Pattern, String> entry : URL_PATTERN_TO_DRIVER.entrySet()) {
            Matcher matcher = entry.getKey().matcher(jdbcUrl);
            if (matcher.find()) {
                String driverClass = entry.getValue();
                logger.info("✅ Resolved driver from URL pattern '{}': {}", 
                    entry.getKey().pattern(), driverClass);
                return Optional.of(driverClass);
            }
        }

        logger.warn("⚠️  Could not resolve driver from URL: {}", jdbcUrl);
        return Optional.empty();
    }

    /**
     * Resolves the JDBC driver class name using multiple strategies:
     * 1. First tries to resolve by explicit database type
     * 2. Falls back to URL pattern matching
     * 
     * @param dbType Database type (can be null)
     * @param jdbcUrl JDBC connection URL (can be null)
     * @return Optional containing the driver class name, or empty if resolution fails
     */
    public Optional<String> resolveDriver(String dbType, String jdbcUrl) {
        // Strategy 1: Try by database type
        Optional<String> driverByType = resolveDriverByType(dbType);
        if (driverByType.isPresent()) {
            return driverByType;
        }

        // Strategy 2: Try by URL pattern
        return resolveDriverByUrl(jdbcUrl);
    }

    /**
     * Extracts the database type from a JDBC URL
     * 
     * @param jdbcUrl JDBC connection URL
     * @return Optional containing the database type, or empty if extraction fails
     */
    public Optional<String> extractDatabaseType(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            return Optional.empty();
        }

        // Pattern: jdbc:<database_type>:...
        Pattern typePattern = Pattern.compile("^jdbc:([^:]+):", Pattern.CASE_INSENSITIVE);
        Matcher matcher = typePattern.matcher(jdbcUrl);

        if (matcher.find()) {
            String dbType = matcher.group(1).toLowerCase();
            logger.debug("Extracted database type from URL: {}", dbType);
            return Optional.of(dbType);
        }

        return Optional.empty();
    }

    /**
     * Returns a map of all supported database types and their driver classes
     * 
     * @return Unmodifiable map of database types to driver classes
     */
    public Map<String, String> getSupportedDatabases() {
        return Map.copyOf(DB_TYPE_TO_DRIVER);
    }
}
