# PromptToSql - MCP Server

## Description

**MCP Server (Model Context Protocol)** is a Spring Boot 3+ application that implements a standardized protocol for LLM agents to interact with database systems via JDBC. This server acts as a bridge, transforming static AI knowledge into a dynamic agent capable of retrieving current information and performing actions.

## Key Features

- **Database Agnostic**: Supports Oracle, MySQL, PostgreSQL, and MSSQL
- **JSON-RPC 2.0 Transport**: Standard communication protocol for MCP
- **Schema Introspection**: Complete database metadata discovery
- **Secure Querying**: SQL injection prevention with READ-ONLY enforcement
- **Trigger Discovery**: Database trigger introspection
- **Caching**: Performance optimization for metadata operations

## Technologies

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring JDBC** (JdbcTemplate - NO JPA)
- **JSON-RPC 2.0**
- **Maven**

## Architecture

```
com.magacho.aiToSql
├── AiToSqlApplication.java          # Main application
├── config/
│   ├── CachingConfig.java           # Cache configuration
│   └── McpServerConfig.java         # MCP server settings
├── controller/
│   └── McpController.java           # JSON-RPC 2.0 endpoint
├── service/
│   ├── SchemaIntrospectionService.java
│   ├── TableDetailsService.java
│   ├── TriggerService.java
│   └── SecureQueryService.java
├── tools/
│   └── McpToolsRegistry.java        # MCP tools registry
├── jsonrpc/
│   ├── JsonRpcRequest.java
│   ├── JsonRpcResponse.java
│   └── JsonRpcError.java
└── dto/
    ├── SchemaStructure.java
    ├── TableDetails.java
    ├── TriggerList.java
    └── QueryResult.java
```

## Prerequisites

1. **Java 21+** installed
2. **Maven 3.6+** installed
3. **Database** with READ-ONLY user configured

## Configuration

### 1. Add JDBC Driver

Edit `pom.xml` and uncomment the appropriate driver:

```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# PostgreSQL Example
spring.datasource.url=jdbc:postgresql://localhost:5432/production_db
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=mcp_readonly_user
spring.datasource.password=secure_password
```

**CRITICAL SECURITY**: The database user MUST have READ-ONLY permissions (SELECT only). This is the primary defense against SQL injection.

## Build and Run

```bash
# Compile
mvn clean install

# Run
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

## MCP Tools

The server exposes 4 tools via JSON-RPC 2.0:

### 1. getSchemaStructure

Get complete database schema with tables and columns.

**Parameters:**
- `databaseName` (optional): Database name

**Returns:** Complete schema structure

### 2. getTableDetails

Get detailed information about a specific table.

**Parameters:**
- `tableName` (required): Table name

**Returns:** Table details including indexes, foreign keys, and constraints

### 3. listTriggers

List all triggers for a specific table.

**Parameters:**
- `tableName` (required): Table name

**Returns:** List of triggers with their definitions

### 4. secureDatabaseQuery

Execute a secure SELECT query.

**Parameters:**
- `queryDescription` (required): SQL SELECT query
- `maxRows` (optional): Maximum rows to return

**Returns:** Query results with metadata

**Security:** Only SELECT statements allowed. Automatically validates and prevents dangerous operations.

## JSON-RPC 2.0 Examples

### Initialize Session

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "params": {},
    "id": 1
  }'
```

### List Available Tools

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "params": {},
    "id": 2
  }'
```

### Get Schema Structure

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "getSchemaStructure",
      "arguments": {
        "databaseName": "mydb"
      }
    },
    "id": 3
  }'
```

### Execute Query

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "secureDatabaseQuery",
      "arguments": {
        "queryDescription": "SELECT * FROM customers WHERE age > 50",
        "maxRows": 100
      }
    },
    "id": 4
  }'
```

## Security Features

- ✅ **READ-ONLY database user** (primary defense)
- ✅ **SELECT-only validation** (rejects INSERT/UPDATE/DELETE/DROP)
- ✅ **Dangerous keyword filtering**
- ✅ **Query result limits** (prevents resource exhaustion)
- ✅ **Comprehensive logging** for audit trails

## Benefits of MCP Implementation

1. **Dynamic Agent**: LLM goes beyond static knowledge to access real-time data
2. **Standardization**: Common protocol for LLM-database interaction
3. **Hallucination Reduction**: Access to trusted external data sources
4. **Scalability**: Can be deployed to Cloud Run, GKE, or other platforms
5. **Multi-Database**: Single interface for Oracle, MySQL, PostgreSQL, MSSQL

## Deployment

### Docker (Optional)

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/aiToSql-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Cloud Run / GKE

The server is stateless and can be easily deployed to managed platforms like Google Cloud Run or GKE, connecting to Cloud SQL instances.

## License

Open source project.

## Group ID / Artifact ID

- **Group ID**: `com.magacho`
- **Artifact ID**: `aiToSql`

