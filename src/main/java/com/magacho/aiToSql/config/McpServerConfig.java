package com.magacho.aiToSql.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcp")
public class McpServerConfig {

    private Server server = new Server();
    private int maxQueryRows = 1000;
    private boolean enableQueryLogging = true;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public int getMaxQueryRows() {
        return maxQueryRows;
    }

    public void setMaxQueryRows(int maxQueryRows) {
        this.maxQueryRows = maxQueryRows;
    }

    public boolean isEnableQueryLogging() {
        return enableQueryLogging;
    }

    public void setEnableQueryLogging(boolean enableQueryLogging) {
        this.enableQueryLogging = enableQueryLogging;
    }

    public static class Server {
        private String name = "Database Introspection MCP Server";
        private String version = "1.0.0";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
