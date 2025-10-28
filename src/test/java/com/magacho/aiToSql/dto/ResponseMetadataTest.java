package com.magacho.aiToSql.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseMetadataTest {

    @Test
    void testEstimateTokens_EmptyString() {
        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens("");
        
        assertEquals(0, tokenInfo.estimated());
        assertEquals("character_count_div_4", tokenInfo.approximationMethod());
        assertNotNull(tokenInfo.warning());
    }

    @Test
    void testEstimateTokens_NullString() {
        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens(null);
        
        assertEquals(0, tokenInfo.estimated());
    }

    @Test
    void testEstimateTokens_SimpleText() {
        // "Hello" = 5 characters → ~2 tokens (ceil(5/4) = 2)
        String text = "Hello";
        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens(text);
        
        assertEquals(2, tokenInfo.estimated());
        assertEquals("character_count_div_4", tokenInfo.approximationMethod());
    }

    @Test
    void testEstimateTokens_JsonText() {
        String json = "{\"name\":\"John\",\"age\":30}";
        // 24 characters → ~6 tokens (ceil(24/4) = 6)
        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens(json);
        
        assertEquals(6, tokenInfo.estimated());
    }

    @Test
    void testEstimateTokens_LargeText() {
        // 1000 characters → 250 tokens
        String largeText = "A".repeat(1000);
        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens(largeText);
        
        assertEquals(250, tokenInfo.estimated());
    }

    @Test
    void testExtractDataInfo_FromQueryResult() {
        QueryResult queryResult = new QueryResult(
                "SELECT * FROM users",
                100,
                1000,
                List.of("id", "name", "email"),
                List.of(
                        Map.of("id", 1, "name", "Alice", "email", "alice@example.com"),
                        Map.of("id", 2, "name", "Bob", "email", "bob@example.com")
                )
        );

        ResponseMetadata.DataInfo dataInfo = ResponseMetadata.extractDataInfo(queryResult);

        assertNotNull(dataInfo);
        assertEquals(100, dataInfo.rowCount());
        assertEquals(3, dataInfo.columnCount());
        assertFalse(dataInfo.truncated()); // rowCount < 1000
        assertEquals(1000, dataInfo.maxRowsLimit());
    }

    @Test
    void testExtractDataInfo_TruncatedResult() {
        QueryResult queryResult = new QueryResult(
                "SELECT * FROM large_table",
                1000, // Exactly at limit
                1000,
                List.of("id", "data"),
                List.of() // Data doesn't matter for this test
        );

        ResponseMetadata.DataInfo dataInfo = ResponseMetadata.extractDataInfo(queryResult);

        assertNotNull(dataInfo);
        assertTrue(dataInfo.truncated()); // rowCount >= 1000
    }

    @Test
    void testExtractDataInfo_NonQueryResult() {
        SchemaStructure schema = new SchemaStructure("TestDB", "PostgreSQL", List.of());

        ResponseMetadata.DataInfo dataInfo = ResponseMetadata.extractDataInfo(schema);

        assertNull(dataInfo); // Should return null for non-QueryResult objects
    }

    @Test
    void testCreate_CompleteMetadata() {
        QueryResult queryResult = new QueryResult(
                "SELECT id, name FROM users LIMIT 10",
                10,
                1000,
                List.of("id", "name"),
                List.of(
                        Map.of("id", 1, "name", "Alice"),
                        Map.of("id", 2, "name", "Bob")
                )
        );

        long executionTime = 45L;
        boolean cached = false;

        ResponseMetadata metadata = ResponseMetadata.create(queryResult, executionTime, cached);

        // Verify tokens
        assertNotNull(metadata.tokens());
        assertTrue(metadata.tokens().estimated() > 0);
        assertEquals("character_count_div_4", metadata.tokens().approximationMethod());

        // Verify performance
        assertNotNull(metadata.performance());
        assertEquals(45L, metadata.performance().executionTimeMs());
        assertFalse(metadata.performance().cachedResult());

        // Verify data info
        assertNotNull(metadata.data());
        assertEquals(10, metadata.data().rowCount());
        assertEquals(2, metadata.data().columnCount());
        assertFalse(metadata.data().truncated());
    }

    @Test
    void testCreate_WithCachedResult() {
        SchemaStructure schema = new SchemaStructure(
                "testdb",
                "PostgreSQL",
                List.of()
        );

        ResponseMetadata metadata = ResponseMetadata.create(schema, 5L, true);

        assertNotNull(metadata.performance());
        assertEquals(5L, metadata.performance().executionTimeMs());
        assertTrue(metadata.performance().cachedResult());
    }

    @Test
    void testTokenEstimationAccuracy() {
        // Test with realistic JSON response
        String jsonResponse = """
                {
                  "query": "SELECT * FROM customers WHERE country = 'Brazil'",
                  "rowCount": 125,
                  "columnNames": ["id", "name", "email", "country"],
                  "data": [
                    {"id": 1, "name": "João Silva", "email": "joao@example.com", "country": "Brazil"},
                    {"id": 2, "name": "Maria Santos", "email": "maria@example.com", "country": "Brazil"}
                  ]
                }
                """;

        ResponseMetadata.TokenInfo tokenInfo = ResponseMetadata.estimateTokens(jsonResponse);

        // Character count: ~331 characters
        // Expected tokens: ~83 (331/4)
        assertTrue(tokenInfo.estimated() >= 75 && tokenInfo.estimated() <= 90,
                "Token estimation should be close to 83 for this JSON (~331 chars)");
    }

    @Test
    void testRecordImmutability() {
        ResponseMetadata.TokenInfo tokenInfo = new ResponseMetadata.TokenInfo(
                100,
                "test_method",
                "test warning"
        );

        // Records are immutable - values shouldn't change
        assertEquals(100, tokenInfo.estimated());
        assertEquals("test_method", tokenInfo.approximationMethod());
        assertEquals("test warning", tokenInfo.warning());
    }

    @Test
    void testPerformanceInfoRecord() {
        ResponseMetadata.PerformanceInfo perfInfo = 
                new ResponseMetadata.PerformanceInfo(250L, true);

        assertEquals(250L, perfInfo.executionTimeMs());
        assertTrue(perfInfo.cachedResult());
    }

    @Test
    void testDataInfoRecord() {
        ResponseMetadata.DataInfo dataInfo = 
                new ResponseMetadata.DataInfo(50, 5, false, 1000);

        assertEquals(50, dataInfo.rowCount());
        assertEquals(5, dataInfo.columnCount());
        assertFalse(dataInfo.truncated());
        assertEquals(1000, dataInfo.maxRowsLimit());
    }

    @Test
    void testResponseMetadataRecord() {
        ResponseMetadata.TokenInfo tokens = new ResponseMetadata.TokenInfo(100, "method", "warning");
        ResponseMetadata.PerformanceInfo perf = new ResponseMetadata.PerformanceInfo(50L, false);
        ResponseMetadata.DataInfo data = new ResponseMetadata.DataInfo(10, 3, false, 1000);

        ResponseMetadata metadata = new ResponseMetadata(tokens, perf, data);

        assertEquals(tokens, metadata.tokens());
        assertEquals(perf, metadata.performance());
        assertEquals(data, metadata.data());
    }
}
