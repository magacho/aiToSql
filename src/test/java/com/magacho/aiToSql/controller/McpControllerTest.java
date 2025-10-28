package com.magacho.aiToSql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magacho.aiToSql.jsonrpc.JsonRpcRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for MCP Controller
 * Tests JSON-RPC 2.0 protocol implementation
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"}, 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:cleanup.sql", 
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("McpController Integration Tests")
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /mcp should return server info")
    void testGetServerInfo() throws Exception {
        mockMvc.perform(get("/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test MCP Server"))
                .andExpect(jsonPath("$.version").value("1.0.0-TEST"))
                .andExpect(jsonPath("$.protocol").value("JSON-RPC 2.0"))
                .andExpect(jsonPath("$.status").value("running"));
    }

    @Test
    @DisplayName("POST /mcp with initialize should return server capabilities")
    void testInitialize() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("initialize", Map.of(), 1);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.result.protocolVersion").exists())
                .andExpect(jsonPath("$.result.serverInfo.name").exists())
                .andExpect(jsonPath("$.result.capabilities.tools").exists());
    }

    @Test
    @DisplayName("POST /mcp with tools/list should return available tools")
    void testListTools() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("tools/list", Map.of(), 2);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.result.tools").isArray())
                .andExpect(jsonPath("$.result.tools", hasSize(4)))
                .andExpect(jsonPath("$.result.tools[*].name", 
                        containsInAnyOrder("getSchemaStructure", "getTableDetails", 
                                          "listTriggers", "secureDatabaseQuery")));
    }

    @Test
    @DisplayName("POST /mcp with ping should return status ok")
    void testPing() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("ping", Map.of(), 3);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.result.status").value("ok"));
    }

    @Test
    @DisplayName("POST /mcp with invalid JSON-RPC version should return error")
    void testInvalidJsonRpcVersion() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "jsonrpc": "1.0",
                    "method": "ping",
                    "id": 4
                }
                """;

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value(-32600))
                .andExpect(jsonPath("$.error.message").value("Invalid JSON-RPC version"));
    }

    @Test
    @DisplayName("POST /mcp with unknown method should return method not found error")
    void testUnknownMethod() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("unknownMethod", Map.of(), 5);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value(-32601));
    }

    @Test
    @DisplayName("POST /mcp with tools/call and invalid params should return error")
    void testToolsCallWithInvalidParams() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("tools/call", 
                Map.of("name", "getTableDetails"), // Missing required tableName
                6);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When / Then
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error").exists());
    }
}
