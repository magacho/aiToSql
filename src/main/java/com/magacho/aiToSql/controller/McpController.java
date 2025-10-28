package com.magacho.aiToSql.controller;

import com.magacho.aiToSql.config.McpServerConfig;
import com.magacho.aiToSql.jsonrpc.JsonRpcError;
import com.magacho.aiToSql.jsonrpc.JsonRpcRequest;
import com.magacho.aiToSql.jsonrpc.JsonRpcResponse;
import com.magacho.aiToSql.tools.McpToolsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP Server Controller
 * Implements JSON-RPC 2.0 transport layer for Model Context Protocol
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final McpToolsRegistry toolsRegistry;
    private final McpServerConfig config;

    public McpController(McpToolsRegistry toolsRegistry, McpServerConfig config) {
        this.toolsRegistry = toolsRegistry;
        this.config = config;
    }

    /**
     * Main JSON-RPC 2.0 endpoint
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> handleJsonRpc(@RequestBody JsonRpcRequest request) {
        log.info("Received JSON-RPC request: method={}, id={}", request.getMethod(), request.getId());

        try {
            // Validate JSON-RPC version
            if (!"2.0".equals(request.getJsonrpc())) {
                return ResponseEntity.ok(new JsonRpcResponse(
                        new JsonRpcError(JsonRpcError.INVALID_REQUEST, "Invalid JSON-RPC version"),
                        request.getId()
                ));
            }

            // Route to appropriate handler
            Object result = switch (request.getMethod()) {
                case "initialize" -> handleInitialize();
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolsCall(request.getParams());
                case "ping" -> handlePing();
                default -> throw new IllegalArgumentException("Method not found: " + request.getMethod());
            };

            return ResponseEntity.ok(new JsonRpcResponse(result, request.getId()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.METHOD_NOT_FOUND, e.getMessage()),
                    request.getId()
            ));
        } catch (SecurityException e) {
            log.error("Security violation: {}", e.getMessage());
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(-32001, "Security violation: " + e.getMessage()),
                    request.getId()
            ));
        } catch (Exception e) {
            log.error("Internal error processing request", e);
            return ResponseEntity.ok(new JsonRpcResponse(
                    new JsonRpcError(JsonRpcError.INTERNAL_ERROR, "Internal server error: " + e.getMessage()),
                    request.getId()
            ));
        }
    }

    /**
     * Initialize MCP session
     */
    private Map<String, Object> handleInitialize() {
        Map<String, Object> response = new HashMap<>();
        response.put("protocolVersion", "2024-11-05");
        response.put("serverInfo", Map.of(
                "name", config.getServer().getName(),
                "version", config.getServer().getVersion()
        ));
        response.put("capabilities", Map.of(
                "tools", Map.of("listChanged", false),
                "resources", Map.of(),
                "prompts", Map.of()
        ));
        return response;
    }

    /**
     * List available MCP tools
     */
    private Map<String, Object> handleToolsList() {
        var toolDefinitions = toolsRegistry.getToolDefinitions();
        
        return Map.of("tools", toolDefinitions.values().stream()
                .map(tool -> Map.of(
                        "name", tool.name(),
                        "description", tool.description(),
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", convertParameters(tool.parameters()),
                                "required", tool.parameters().entrySet().stream()
                                        .filter(e -> e.getValue().required())
                                        .map(Map.Entry::getKey)
                                        .toList()
                        )
                ))
                .toList()
        );
    }

    private Map<String, Map<String, String>> convertParameters(
            Map<String, McpToolsRegistry.ParameterDefinition> parameters) {
        Map<String, Map<String, String>> result = new HashMap<>();
        parameters.forEach((name, param) -> {
            result.put(name, Map.of(
                    "type", param.type(),
                    "description", param.description()
            ));
        });
        return result;
    }

    /**
     * Execute MCP tool
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolsCall(Object params) {
        if (!(params instanceof Map)) {
            throw new IllegalArgumentException("Invalid params format");
        }

        Map<String, Object> paramsMap = (Map<String, Object>) params;
        String toolName = (String) paramsMap.get("name");
        Map<String, Object> arguments = (Map<String, Object>) paramsMap.getOrDefault("arguments", Map.of());

        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }

        Object result = toolsRegistry.executeTool(toolName, arguments);

        return Map.of(
                "content", java.util.List.of(Map.of(
                        "type", "text",
                        "text", convertResultToText(result)
                )),
                "isError", false
        );
    }

    /**
     * Health check endpoint
     */
    private Map<String, String> handlePing() {
        return Map.of("status", "ok", "server", config.getServer().getName());
    }

    /**
     * Convert result object to text representation for LLM
     */
    private String convertResultToText(Object result) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
        } catch (Exception e) {
            return result.toString();
        }
    }

    /**
     * Simple GET endpoint for server info
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        return ResponseEntity.ok(Map.of(
                "name", config.getServer().getName(),
                "version", config.getServer().getVersion(),
                "protocol", "JSON-RPC 2.0",
                "endpoint", "/mcp",
                "status", "running"
        ));
    }
}
