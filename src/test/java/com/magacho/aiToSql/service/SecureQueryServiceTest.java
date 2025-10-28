package com.magacho.aiToSql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests for SecureQueryService
 * Tests SQL injection prevention and query validation
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
@DisplayName("SecureQueryService Integration Tests")
class SecureQueryServiceTest {

    @Autowired
    private SecureQueryService secureQueryService;

    @Test
    @DisplayName("Should validate SELECT queries as safe")
    void testValidateSelectQuery() {
        // Given
        String validQuery = "SELECT * FROM customers";
        
        // When
        boolean isValid = secureQueryService.validateQuery(validQuery);
        
        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject UPDATE queries")
    void testRejectUpdateQuery() {
        // Given
        String updateQuery = "UPDATE customers SET name = 'test'";
        
        // When
        boolean isValid = secureQueryService.validateQuery(updateQuery);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject DELETE queries")
    void testRejectDeleteQuery() {
        // Given
        String deleteQuery = "DELETE FROM customers WHERE id = 1";
        
        // When
        boolean isValid = secureQueryService.validateQuery(deleteQuery);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject DROP queries")
    void testRejectDropQuery() {
        // Given
        String dropQuery = "DROP TABLE customers";
        
        // When
        boolean isValid = secureQueryService.validateQuery(dropQuery);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject INSERT queries")
    void testRejectInsertQuery() {
        // Given
        String insertQuery = "INSERT INTO customers VALUES (1, 'test')";
        
        // When
        boolean isValid = secureQueryService.validateQuery(insertQuery);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject CREATE queries")
    void testRejectCreateQuery() {
        // Given
        String createQuery = "CREATE TABLE test (id INT)";
        
        // When
        boolean isValid = secureQueryService.validateQuery(createQuery);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject queries with SQL injection attempt")
    void testRejectSqlInjection() {
        // Given
        String injectionAttempt = "SELECT * FROM users WHERE id = 1; DROP TABLE users; --";
        
        // When
        boolean isValid = secureQueryService.validateQuery(injectionAttempt);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject null query")
    void testRejectNullQuery() {
        // When
        boolean isValid = secureQueryService.validateQuery(null);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty query")
    void testRejectEmptyQuery() {
        // When
        boolean isValid = secureQueryService.validateQuery("");
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should validate SELECT query with JOIN")
    void testValidateSelectWithJoin() {
        // Given
        String joinQuery = "SELECT c.*, o.* FROM customers c JOIN orders o ON c.id = o.customer_id";
        
        // When
        boolean isValid = secureQueryService.validateQuery(joinQuery);
        
        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should validate SELECT query with WHERE clause")
    void testValidateSelectWithWhere() {
        // Given
        String whereQuery = "SELECT * FROM customers WHERE age > 50 AND country = 'Brazil'";
        
        // When
        boolean isValid = secureQueryService.validateQuery(whereQuery);
        
        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should throw SecurityException for non-SELECT query execution")
    void testSecurityExceptionOnNonSelect() {
        // Given
        String updateQuery = "UPDATE customers SET name = 'hacked'";
        
        // When / Then
        assertThatThrownBy(() -> secureQueryService.secureDatabaseQuery(updateQuery, null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only SELECT queries are allowed");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null query execution")
    void testIllegalArgumentExceptionOnNull() {
        // When / Then
        assertThatThrownBy(() -> secureQueryService.secureDatabaseQuery(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }
}
