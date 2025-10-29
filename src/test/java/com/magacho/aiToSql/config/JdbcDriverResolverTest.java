package com.magacho.aiToSql.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcDriverResolverTest {

    private JdbcDriverResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new JdbcDriverResolver();
    }

    @Test
    void shouldResolvePostgreSQLDriverByType() {
        Optional<String> driver = resolver.resolveDriverByType("postgresql");
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.postgresql.Driver");
    }

    @Test
    void shouldResolvePostgreSQLDriverByCaseInsensitiveType() {
        assertThat(resolver.resolveDriverByType("POSTGRESQL")).isPresent();
        assertThat(resolver.resolveDriverByType("PostgreSQL")).isPresent();
        assertThat(resolver.resolveDriverByType("postgreSql")).isPresent();
    }

    @Test
    void shouldResolveMySQLDriverByType() {
        Optional<String> driver = resolver.resolveDriverByType("mysql");
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    void shouldResolveSQLServerDriverByType() {
        assertThat(resolver.resolveDriverByType("sqlserver"))
            .hasValue("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        assertThat(resolver.resolveDriverByType("mssql"))
            .hasValue("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Test
    void shouldResolveOracleDriverByType() {
        Optional<String> driver = resolver.resolveDriverByType("oracle");
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("oracle.jdbc.OracleDriver");
    }

    @Test
    void shouldResolveH2DriverByType() {
        Optional<String> driver = resolver.resolveDriverByType("h2");
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.h2.Driver");
    }

    @Test
    void shouldReturnEmptyForUnknownType() {
        Optional<String> driver = resolver.resolveDriverByType("unknown_db");
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullType() {
        Optional<String> driver = resolver.resolveDriverByType(null);
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldReturnEmptyForEmptyType() {
        Optional<String> driver = resolver.resolveDriverByType("");
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldResolvePostgreSQLDriverByUrl() {
        String url = "jdbc:postgresql://localhost:5432/mydb";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.postgresql.Driver");
    }

    @Test
    void shouldResolveMySQLDriverByUrl() {
        String url = "jdbc:mysql://localhost:3306/mydb";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    void shouldResolveSQLServerDriverByUrl() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=mydb";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Test
    void shouldResolveOracleDriverByUrl() {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("oracle.jdbc.OracleDriver");
    }

    @Test
    void shouldResolveH2DriverByUrl() {
        String url = "jdbc:h2:mem:testdb";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.h2.Driver");
    }

    @Test
    void shouldReturnEmptyForUnknownUrl() {
        String url = "jdbc:unknown://localhost/mydb";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInvalidUrl() {
        String url = "not-a-jdbc-url";
        Optional<String> driver = resolver.resolveDriverByUrl(url);
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullUrl() {
        Optional<String> driver = resolver.resolveDriverByUrl(null);
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldPrioritizeTypeOverUrl() {
        String type = "postgresql";
        String url = "jdbc:mysql://localhost:3306/mydb";
        
        Optional<String> driver = resolver.resolveDriver(type, url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.postgresql.Driver");
    }

    @Test
    void shouldFallbackToUrlWhenTypeIsNull() {
        String url = "jdbc:mysql://localhost:3306/mydb";
        
        Optional<String> driver = resolver.resolveDriver(null, url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    void shouldFallbackToUrlWhenTypeIsUnknown() {
        String type = "unknown_type";
        String url = "jdbc:postgresql://localhost:5432/mydb";
        
        Optional<String> driver = resolver.resolveDriver(type, url);
        
        assertThat(driver).isPresent();
        assertThat(driver.get()).isEqualTo("org.postgresql.Driver");
    }

    @Test
    void shouldReturnEmptyWhenBothTypeAndUrlFail() {
        String type = "unknown_type";
        String url = "jdbc:unknown://localhost/mydb";
        
        Optional<String> driver = resolver.resolveDriver(type, url);
        
        assertThat(driver).isEmpty();
    }

    @Test
    void shouldExtractDatabaseTypeFromPostgreSQLUrl() {
        String url = "jdbc:postgresql://localhost:5432/mydb";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).hasValue("postgresql");
    }

    @Test
    void shouldExtractDatabaseTypeFromMySQLUrl() {
        String url = "jdbc:mysql://localhost:3306/mydb";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).hasValue("mysql");
    }

    @Test
    void shouldExtractDatabaseTypeFromSQLServerUrl() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=mydb";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).hasValue("sqlserver");
    }

    @Test
    void shouldExtractDatabaseTypeFromOracleUrl() {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).hasValue("oracle");
    }

    @Test
    void shouldExtractDatabaseTypeFromH2Url() {
        String url = "jdbc:h2:mem:testdb";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).hasValue("h2");
    }

    @Test
    void shouldReturnEmptyWhenExtractionFails() {
        String url = "not-a-jdbc-url";
        
        Optional<String> dbType = resolver.extractDatabaseType(url);
        
        assertThat(dbType).isEmpty();
    }

    @Test
    void shouldReturnSupportedDatabases() {
        Map<String, String> supported = resolver.getSupportedDatabases();
        
        assertThat(supported)
            .containsEntry("postgresql", "org.postgresql.Driver")
            .containsEntry("mysql", "com.mysql.cj.jdbc.Driver")
            .containsEntry("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
            .containsEntry("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
            .containsEntry("oracle", "oracle.jdbc.OracleDriver")
            .containsEntry("h2", "org.h2.Driver");
    }

    @Test
    void shouldReturnImmutableSupportedDatabases() {
        Map<String, String> supported = resolver.getSupportedDatabases();
        
        assertThat(supported).isUnmodifiable();
    }
}
