package com.magacho.aiToSql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * MCP Server Application - Model Context Protocol Server
 * Provides database introspection and query tools for LLM agents
 */
@SpringBootApplication
@EnableCaching
public class AiToSqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiToSqlApplication.class, args);
        System.out.println("=================================================");
        System.out.println("MCP Server Started - Database Introspection Tool");
        System.out.println("JSON-RPC 2.0 Endpoint: http://localhost:8080/mcp");
        System.out.println("=================================================");
    }
}
