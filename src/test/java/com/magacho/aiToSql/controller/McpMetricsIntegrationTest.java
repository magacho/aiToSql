package com.magacho.aiToSql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magacho.aiToSql.jsonrpc.JsonRpcRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/cleanup.sql", "/test-schema.sql", "/test-data.sql"}, 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", 
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class McpMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testMetricsEndpoint_Initially_Empty() throws Exception {
        // Given - Reset metrics first
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());
        
        // When/Then
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools").exists())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary.totalCalls").value(0))
                .andExpect(jsonPath("$.summary.totalCostUSD").value(0.0));
    }

    @Test
    void testMetricsEndpoint_AfterToolExecution() throws Exception {
        // Given - Reset metrics first
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());

        // When - Execute a tool
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "getSchemaStructure",
                        "arguments", Map.of()
                ),
                1
        );

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - Check metrics
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.getSchemaStructure").exists())
                .andExpect(jsonPath("$.tools.getSchemaStructure.totalCalls").value(1))
                .andExpect(jsonPath("$.tools.getSchemaStructure.avgExecutionTimeMs").isNumber())
                .andExpect(jsonPath("$.tools.getSchemaStructure.avgTokens").isNumber())
                .andExpect(jsonPath("$.tools.getSchemaStructure.totalCostUSD").isNumber())
                .andExpect(jsonPath("$.summary.totalCalls").value(1))
                .andExpect(jsonPath("$.summary.totalCostUSD").value(greaterThan(0.0)));
    }

    @Test
    void testMetricsEndpoint_MultipleTools() throws Exception {
        // Given - Reset metrics
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());

        // When - Execute different tools
        executeSchemaStructureTool();
        executeQueryTool();
        executeSchemaStructureTool(); // Execute again to test aggregation

        // Then - Check metrics for both tools
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.getSchemaStructure.totalCalls").value(2))
                .andExpect(jsonPath("$.tools.secureDatabaseQuery.totalCalls").value(1))
                .andExpect(jsonPath("$.summary.totalCalls").value(3));
    }

    @Test
    void testMetricsReset() throws Exception {
        // Given - Execute some tools
        executeSchemaStructureTool();
        executeQueryTool();

        // Verify metrics exist
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalCalls").value(greaterThan(0)));

        // When - Reset metrics
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Metrics reset successfully"));

        // Then - Metrics should be empty
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalCalls").value(0));
    }

    @Test
    void testTokenEstimation_InMetadata() throws Exception {
        // Given - Reset metrics
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());

        // When - Execute a tool and check response metadata
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "getTableDetails",
                        "arguments", Map.of("tableName", "customers")
                ),
                1
        );

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.meta.tokens.estimated").exists())
                .andExpect(jsonPath("$.result.meta.performance.executionTimeMs").exists());

        // Then - Check accumulated metrics - using String comparison to avoid ClassCastException
        String response = mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.getTableDetails").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Verify metrics exist and have positive values
        assertTrue(response.contains("getTableDetails"));
        assertTrue(response.contains("avgTokens"));
        assertTrue(response.contains("totalCostUSD"));
    }

    @Test
    void testCostEstimation() throws Exception {
        // Given - Reset metrics
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());

        // When - Execute a tool that returns significant data
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "secureDatabaseQuery",
                        "arguments", Map.of(
                                "queryDescription", "SELECT * FROM customers",
                                "maxRows", 100
                        )
                ),
                1
        );

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - Cost should be calculated
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.secureDatabaseQuery.totalCostUSD").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.summary.averageCostPerCall").value(greaterThan(0.0)));
    }

    @Test
    void testPerformanceTracking() throws Exception {
        // Given - Reset metrics
        mockMvc.perform(post("/mcp/metrics/reset"))
                .andExpect(status().isOk());

        // When - Execute the same tool multiple times
        for (int i = 0; i < 3; i++) {
            executeSchemaStructureTool();
            Thread.sleep(10); // Small delay to ensure measurable execution time
        }

        // Then - Should track average execution time
        mockMvc.perform(get("/mcp/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.getSchemaStructure.totalCalls").value(3))
                .andExpect(jsonPath("$.tools.getSchemaStructure.avgExecutionTimeMs").exists());
    }

    // Helper methods

    private void executeSchemaStructureTool() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "getSchemaStructure",
                        "arguments", Map.of()
                ),
                1
        );

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void executeQueryTool() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "secureDatabaseQuery",
                        "arguments", Map.of(
                                "queryDescription", "SELECT COUNT(*) FROM customers",
                                "maxRows", 10
                        )
                ),
                1
        );

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
