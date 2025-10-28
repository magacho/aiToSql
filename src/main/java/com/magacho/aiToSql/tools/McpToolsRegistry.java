package com.magacho.aiToSql.tools;

import com.magacho.aiToSql.dto.QueryResult;
import com.magacho.aiToSql.dto.SchemaStructure;
import com.magacho.aiToSql.dto.TableDetails;
import com.magacho.aiToSql.dto.TriggerList;
import com.magacho.aiToSql.service.SchemaIntrospectionService;
import com.magacho.aiToSql.service.SecureQueryService;
import com.magacho.aiToSql.service.TableDetailsService;
import com.magacho.aiToSql.service.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP Tools Registry
 * Implements the MCP protocol tools for database introspection and querying
 */
@Component
public class McpToolsRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolsRegistry.class);

    private final SchemaIntrospectionService schemaService;
    private final TableDetailsService tableDetailsService;
    private final TriggerService triggerService;
    private final SecureQueryService queryService;

    public McpToolsRegistry(
            SchemaIntrospectionService schemaService,
            TableDetailsService tableDetailsService,
            TriggerService triggerService,
            SecureQueryService queryService) {
        this.schemaService = schemaService;
        this.tableDetailsService = tableDetailsService;
        this.triggerService = triggerService;
        this.queryService = queryService;
    }

    /**
     * Execute MCP tool by name
     */
    public Object executeTool(String toolName, Map<String, Object> params) {
        log.info("Executing MCP tool: {} with params: {}", toolName, params);

        try {
            return switch (toolName) {
                case "getSchemaStructure" -> executeGetSchemaStructure(params);
                case "getTableDetails" -> executeGetTableDetails(params);
                case "listTriggers" -> executeListTriggers(params);
                case "secureDatabaseQuery" -> executeSecureDatabaseQuery(params);
                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Error executing tool {}: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    private SchemaStructure executeGetSchemaStructure(Map<String, Object> params) throws Exception {
        String databaseName = (String) params.getOrDefault("databaseName", "default");
        return schemaService.getSchemaStructure(databaseName);
    }

    private TableDetails executeGetTableDetails(Map<String, Object> params) throws Exception {
        String tableName = (String) params.get("tableName");
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName parameter is required");
        }
        return tableDetailsService.getTableDetails(tableName);
    }

    private TriggerList executeListTriggers(Map<String, Object> params) throws Exception {
        String tableName = (String) params.get("tableName");
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName parameter is required");
        }
        return triggerService.listTriggers(tableName);
    }

    private QueryResult executeSecureDatabaseQuery(Map<String, Object> params) {
        String queryDescription = (String) params.get("queryDescription");
        if (queryDescription == null || queryDescription.isBlank()) {
            throw new IllegalArgumentException("queryDescription parameter is required");
        }

        Integer maxRows = null;
        Object maxRowsParam = params.get("maxRows");
        if (maxRowsParam != null) {
            maxRows = maxRowsParam instanceof Integer 
                    ? (Integer) maxRowsParam 
                    : Integer.parseInt(maxRowsParam.toString());
        }

        return queryService.secureDatabaseQuery(queryDescription, maxRows);
    }

    /**
     * Get tool definitions for MCP protocol
     */
    public Map<String, ToolDefinition> getToolDefinitions() {
        return Map.of(
                "getSchemaStructure", new ToolDefinition(
                        "getSchemaStructure",
                        "Get complete database schema structure with all tables and columns",
                        Map.of("databaseName", new ParameterDefinition("string", "Database name", false))
                ),
                "getTableDetails", new ToolDefinition(
                        "getTableDetails",
                        "Get detailed information about a specific table including indexes, foreign keys, and constraints",
                        Map.of("tableName", new ParameterDefinition("string", "Table name", true))
                ),
                "listTriggers", new ToolDefinition(
                        "listTriggers",
                        "List all triggers defined for a specific table",
                        Map.of("tableName", new ParameterDefinition("string", "Table name", true))
                ),
                "secureDatabaseQuery", new ToolDefinition(
                        "secureDatabaseQuery",
                        "Execute a secure SELECT query on the database. Only SELECT statements are allowed.",
                        Map.of(
                                "queryDescription", new ParameterDefinition("string", "SQL SELECT query or natural language description", true),
                                "maxRows", new ParameterDefinition("integer", "Maximum number of rows to return", false)
                        )
                )
        );
    }

    public record ToolDefinition(
            String name,
            String description,
            Map<String, ParameterDefinition> parameters
    ) {
    }

    public record ParameterDefinition(
            String type,
            String description,
            boolean required
    ) {
    }
}
