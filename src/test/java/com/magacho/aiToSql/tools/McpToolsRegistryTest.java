package com.magacho.aiToSql.tools;

import com.magacho.aiToSql.dto.QueryResult;
import com.magacho.aiToSql.dto.SchemaStructure;
import com.magacho.aiToSql.dto.TableDetails;
import com.magacho.aiToSql.dto.TriggerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests for MCP Tools Registry
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
@DisplayName("McpToolsRegistry Integration Tests")
class McpToolsRegistryTest {

    @Autowired
    private McpToolsRegistry toolsRegistry;

    @Test
    @DisplayName("Should return 4 tool definitions")
    void testGetToolDefinitions() {
        // When
        Map<String, McpToolsRegistry.ToolDefinition> tools = toolsRegistry.getToolDefinitions();

        // Then
        assertThat(tools).hasSize(4);
        assertThat(tools).containsKeys(
                "getSchemaStructure",
                "getTableDetails",
                "listTriggers",
                "secureDatabaseQuery"
        );
    }

    @Test
    @DisplayName("getSchemaStructure tool should have correct definition")
    void testGetSchemaStructureDefinition() {
        // When
        Map<String, McpToolsRegistry.ToolDefinition> tools = toolsRegistry.getToolDefinitions();
        McpToolsRegistry.ToolDefinition tool = tools.get("getSchemaStructure");

        // Then
        assertThat(tool).isNotNull();
        assertThat(tool.name()).isEqualTo("getSchemaStructure");
        assertThat(tool.description()).contains("database schema");
        assertThat(tool.parameters()).containsKey("databaseName");
        assertThat(tool.parameters().get("databaseName").required()).isFalse();
    }

    @Test
    @DisplayName("getTableDetails tool should have correct definition")
    void testGetTableDetailsDefinition() {
        // When
        Map<String, McpToolsRegistry.ToolDefinition> tools = toolsRegistry.getToolDefinitions();
        McpToolsRegistry.ToolDefinition tool = tools.get("getTableDetails");

        // Then
        assertThat(tool).isNotNull();
        assertThat(tool.name()).isEqualTo("getTableDetails");
        assertThat(tool.parameters()).containsKey("tableName");
        assertThat(tool.parameters().get("tableName").required()).isTrue();
    }

    @Test
    @DisplayName("secureDatabaseQuery tool should have correct definition")
    void testSecureDatabaseQueryDefinition() {
        // When
        Map<String, McpToolsRegistry.ToolDefinition> tools = toolsRegistry.getToolDefinitions();
        McpToolsRegistry.ToolDefinition tool = tools.get("secureDatabaseQuery");

        // Then
        assertThat(tool).isNotNull();
        assertThat(tool.name()).isEqualTo("secureDatabaseQuery");
        assertThat(tool.parameters()).containsKeys("queryDescription", "maxRows");
        assertThat(tool.parameters().get("queryDescription").required()).isTrue();
        assertThat(tool.parameters().get("maxRows").required()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for unknown tool")
    void testUnknownTool() {
        // When / Then
        assertThatThrownBy(() -> 
                toolsRegistry.executeTool("unknownTool", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown tool");
    }

    @Test
    @DisplayName("Should throw exception when required parameter is missing")
    void testMissingRequiredParameter() {
        // When / Then
        assertThatThrownBy(() -> 
                toolsRegistry.executeTool("getTableDetails", Map.of()))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("tableName");
    }
}
