package com.magacho.aiToSql.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magacho.aiToSql.jsonrpc.JsonRpcRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test
 * 
 * Simula a jornada completa de um LLM interagindo com o MCP Server:
 * 1. Inicializa a sessão MCP
 * 2. Lista as ferramentas disponíveis
 * 3. Obtém o schema do banco de dados (getSchemaStructure)
 * 4. Faz perguntas sobre os dados usando linguagem natural (secureDatabaseQuery)
 * 5. Valida que as respostas são tokenizadas com métricas de performance
 * 6. Verifica os gastos de recursos computacionais
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = {"/cleanup.sql", "/test-schema.sql", "/test-data.sql"}, 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/cleanup.sql", 
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class EndToEndJourneyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MCP_ENDPOINT = "/mcp";
    
    @BeforeAll
    static void setUpOnce() {
        System.out.println("\n========================================");
        System.out.println("  END-TO-END JOURNEY TEST STARTING");
        System.out.println("========================================\n");
    }

    /**
     * Etapa 1: Inicialização da sessão MCP
     * O LLM deve primeiro inicializar a sessão para descobrir as capacidades do servidor
     */
    @Test
    @Order(1)
    @DisplayName("Step 1: LLM initializes MCP session and discovers capabilities")
    void step1_initializeMcpSession() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("initialize", Map.of(), 1);

        // When
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.result.protocolVersion").exists())
                .andExpect(jsonPath("$.result.serverInfo.name").value("Test MCP Server"))
                .andExpect(jsonPath("$.result.serverInfo.version").exists())
                .andExpect(jsonPath("$.result.capabilities.tools").exists())
                .andReturn();

        // Then - Log the initialization response
        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== MCP Session Initialized ===");
        System.out.println(response);
    }

    /**
     * Etapa 2: Descoberta das ferramentas disponíveis
     * O LLM lista todas as ferramentas que pode invocar
     */
    @Test
    @Order(2)
    @DisplayName("Step 2: LLM discovers available tools")
    void step2_discoverAvailableTools() throws Exception {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("tools/list", Map.of(), 2);

        // When
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools").isArray())
                .andExpect(jsonPath("$.result.tools", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.result.tools[*].name", 
                        hasItems("getSchemaStructure", "getTableDetails", "listTriggers", "secureDatabaseQuery")))
                .andReturn();

        // Then - Log available tools
        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== Available Tools ===");
        System.out.println(response);
    }

    /**
     * Etapa 3: Compreensão do Modelo de Dados
     * O LLM invoca getSchemaStructure para entender a estrutura do banco
     */
    @Test
    @Order(3)
    @DisplayName("Step 3: LLM understands the database model via getSchemaStructure")
    void step3_understandDatabaseModel() throws Exception {
        // Given - LLM wants to understand the database structure
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "getSchemaStructure",
                        "arguments", Map.of()
                ),
                3
        );

        // When
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].type").value("text"))
                .andExpect(jsonPath("$.result.content[0].text").exists())
                .andExpect(jsonPath("$.result.isError").value(false))
                .andExpect(jsonPath("$.result.meta.tokens.estimated").isNumber())
                .andExpect(jsonPath("$.result.meta.tokens.inputTokens").isNumber())
                .andExpect(jsonPath("$.result.meta.tokens.outputTokens").isNumber())
                .andExpect(jsonPath("$.result.meta.performance.executionTimeMs").isNumber())
                .andExpect(jsonPath("$.result.meta.cost.estimatedUSD").isNumber())
                .andReturn();
        
        long executionTime = System.currentTimeMillis() - startTime;

        // Then - Validate response structure and content
        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) jsonResponse.get("result");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) resultMap.get("meta");
        
        System.out.println("\n=== Database Schema Retrieved ===");
        System.out.println("Schema content length: " + 
                ((Map<?, ?>) ((java.util.List<?>) resultMap.get("content")).get(0)).get("text").toString().length());
        System.out.println("Tokens: " + meta.get("tokens"));
        System.out.println("Performance: " + meta.get("performance"));
        System.out.println("Cost: " + meta.get("cost"));
        System.out.println("Execution time: " + executionTime + "ms");
        
        // Assert the schema contains table information
        @SuppressWarnings("unchecked")
        String schemaText = (String) ((Map<?, ?>) ((java.util.List<?>) resultMap.get("content")).get(0)).get("text");
        assertTrue(schemaText.contains("CUSTOMERS") || schemaText.contains("customers"), 
                "Schema should contain customers table");
    }

    /**
     * Etapa 4: Obter detalhes de uma tabela específica
     * O LLM quer entender melhor uma tabela específica
     */
    @Test
    @Order(4)
    @DisplayName("Step 4: LLM gets detailed information about a specific table")
    void step4_getTableDetails() throws Exception {
        // Given - LLM wants details about the customers table
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "getTableDetails",
                        "arguments", Map.of("tableName", "customers")
                ),
                4
        );

        // When
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text").exists())
                .andExpect(jsonPath("$.result.meta.tokens.estimated").isNumber())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== Table Details Retrieved ===");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        String tableDetails = (String) ((Map<?, ?>) 
                ((java.util.List<?>) ((Map<?, ?>) jsonResponse.get("result")).get("content")).get(0)).get("text");
        
        System.out.println(tableDetails.substring(0, Math.min(500, tableDetails.length())));
        
        assertTrue(tableDetails.contains("customers") || tableDetails.contains("CUSTOMERS"), 
                "Table details should contain table name");
    }

    /**
     * Etapa 5: Pergunta simples em linguagem natural
     * O LLM usa a ferramenta secureDatabaseQuery para responder uma pergunta
     */
    @Test
    @Order(5)
    @DisplayName("Step 5: LLM answers a simple question using natural language query")
    void step5_answerSimpleQuestion() throws Exception {
        // Given - User asks: "Quantos clientes temos?"
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "secureDatabaseQuery",
                        "arguments", Map.of(
                                "queryDescription", "SELECT COUNT(*) as total FROM customers",
                                "maxRows", 10
                        )
                ),
                5
        );

        // When
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text").exists())
                .andExpect(jsonPath("$.result.meta.tokens.estimated").isNumber())
                .andExpect(jsonPath("$.result.meta.performance.executionTimeMs").isNumber())
                .andReturn();
        
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== Query Result for 'How many customers?' ===");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        String queryResult = (String) ((Map<?, ?>) 
                ((java.util.List<?>) ((Map<?, ?>) jsonResponse.get("result")).get("content")).get(0)).get("text");
        
        System.out.println(queryResult);
        System.out.println("Query execution time: " + executionTime + "ms");
        
        assertTrue(queryResult.contains("total") || queryResult.contains("TOTAL"), 
                "Result should contain count");
    }

    /**
     * Etapa 6: Pergunta complexa com dados detalhados
     * O LLM busca informações mais complexas
     */
    @Test
    @Order(6)
    @DisplayName("Step 6: LLM executes a complex query to get detailed customer data")
    void step6_executeComplexQuery() throws Exception {
        // Given - User asks: "Liste os clientes e seus pedidos"
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "secureDatabaseQuery",
                        "arguments", Map.of(
                                "queryDescription", 
                                "SELECT c.name, c.email, COUNT(o.id) as order_count " +
                                "FROM customers c LEFT JOIN orders o ON c.id = o.customer_id " +
                                "GROUP BY c.id, c.name, c.email",
                                "maxRows", 100
                        )
                ),
                6
        );

        // When
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text").exists())
                .andExpect(jsonPath("$.result.meta.tokens.outputTokens").isNumber())
                .andReturn();
        
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== Complex Query Result ===");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        String queryResult = (String) ((Map<?, ?>) 
                ((java.util.List<?>) ((Map<?, ?>) jsonResponse.get("result")).get("content")).get(0)).get("text");
        
        System.out.println(queryResult.substring(0, Math.min(1000, queryResult.length())));
        System.out.println("Query execution time: " + executionTime + "ms");
        
        assertTrue(queryResult.contains("name") || queryResult.contains("NAME"), 
                "Result should contain customer data");
    }

    /**
     * Etapa 7: Validação de segurança - tentativa de executar comando perigoso
     * O sistema deve bloquear comandos não-SELECT
     */
    @Test
    @Order(7)
    @DisplayName("Step 7: Security validation - system blocks dangerous SQL commands")
    void step7_securityValidation() throws Exception {
        // Given - Tentativa maliciosa de fazer DROP TABLE
        JsonRpcRequest request = new JsonRpcRequest(
                "tools/call",
                Map.of(
                        "name", "secureDatabaseQuery",
                        "arguments", Map.of(
                                "queryDescription", "DROP TABLE customers",
                                "maxRows", 10
                        )
                ),
                7
        );

        // When/Then - Should reject non-SELECT commands
        MvcResult result = mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println("\n=== Security Block ===");
        System.out.println(response);
        
        assertTrue(response.contains("error"), "Should contain error for dangerous command");
    }

    /**
     * Etapa 8: Análise final de métricas e custos
     * Verifica o consumo total de recursos da jornada
     */
    @Test
    @Order(8)
    @DisplayName("Step 8: Analyze complete journey metrics and computational costs")
    void step8_analyzeJourneyMetrics() throws Exception {
        // Given - Multiple tools have been executed throughout the journey
        
        // When
        MvcResult result = mockMvc.perform(get(MCP_ENDPOINT + "/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools").exists())
                .andExpect(jsonPath("$.summary.totalCalls").value(greaterThan(0)))
                .andExpect(jsonPath("$.summary.totalCostUSD").value(greaterThan(0.0)))
                .andReturn();

        // Then - Print comprehensive metrics
        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = objectMapper.readValue(response, Map.class);
        
        System.out.println("\n=== JOURNEY COMPLETE - PERFORMANCE METRICS ===");
        System.out.println("\nPer-Tool Statistics:");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> toolMetrics = (Map<String, Object>) metrics.get("tools");
        toolMetrics.forEach((toolName, stats) -> {
            System.out.println("\n" + toolName + ":");
            System.out.println("  " + stats);
        });
        
        System.out.println("\nSummary:");
        System.out.println("  " + metrics.get("summary"));
        
        // Validate key metrics exist
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) metrics.get("summary");
        assertTrue(((Number) summary.get("totalCalls")).intValue() > 0, 
                "Should have recorded tool calls");
        assertTrue(((Number) summary.get("totalCostUSD")).doubleValue() > 0, 
                "Should have estimated costs");
    }

    /**
     * Teste adicional: Ping para verificar saúde do servidor
     */
    @Test
    @DisplayName("Health Check: Server responds to ping")
    void healthCheck_serverPing() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest("ping", Map.of(), 99);

        mockMvc.perform(post(MCP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("ok"));
    }
}
