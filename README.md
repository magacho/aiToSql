# PromptToSql - MCP Server

## Description

**MCP Server (Model Context Protocol)** is a Spring Boot 3+ application that implements a standardized protocol for LLM agents to interact with database systems via JDBC. This server acts as a bridge, transforming static AI knowledge into a dynamic agent capable of retrieving current information and performing actions.

## Key Features

- **Database Agnostic**: Supports Oracle, MySQL, PostgreSQL, and MSSQL
- **🎯 Automatic Driver Detection**: Smart JDBC driver resolution from URL patterns
- **JSON-RPC 2.0 Transport**: Standard communication protocol for MCP
- **Schema Introspection**: Complete database metadata discovery
- **Secure Querying**: SQL injection prevention with READ-ONLY enforcement
- **Trigger Discovery**: Database trigger introspection
- **Caching**: Performance optimization for metadata operations
- **Performance Metrics**: Detailed tokenization and performance tracking
- **Cost Estimation**: Automatic LLM API cost calculation

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

## 🚀 Quick Start

### Option 1: Docker (Recommended) 🐳

The easiest way to run the MCP Server is using Docker:

```bash
# Pull the image from Docker Hub
docker pull flaviomagacho/aitosql:latest

# Run with PostgreSQL
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:postgresql://your-host:5432/your_db" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest

# Test the server
curl http://localhost:8080/actuator/health
curl http://localhost:8080/mcp/tools/list
```

> **✨ New Feature**: No need to specify `DB_TYPE` or `DB_DRIVER` - they are automatically detected! See [JDBC Driver Auto-Detection](JDBC_DRIVER_AUTO_DETECTION.md)

**Or use Docker Compose for local development**:

```bash
# Clone the repository
git clone https://github.com/magacho/aiToSql.git
cd aiToSql

# Start with PostgreSQL (or mysql, sqlserver)
docker-compose -f docker-compose-postgres.yml up -d

# View logs
docker-compose -f docker-compose-postgres.yml logs -f mcp-server

# Stop
docker-compose -f docker-compose-postgres.yml down
```

📖 **Full Documentation**:
- **[Docker Deployment Guide](DOCKER_DEPLOYMENT.md)** - Complete deployment instructions
- **[Docker Build Guide](DOCKER_BUILD_GUIDE.md)** - How to build and publish
- **[Quick Start Guide](QUICKSTART.md)** - Step-by-step tutorial

---

### Option 2: Build from Source

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

## 📊 Testing & Coverage

This project maintains **comprehensive test coverage** with **no mandatory minimum**. We focus on tracking coverage evolution across releases:

- **Current Coverage**: Check [COVERAGE_REPORT.md](COVERAGE_REPORT.md)
- **Coverage History**: See [RELEASE_HISTORY.md](RELEASE_HISTORY.md)
- **Testing Guide**: Read [TESTING_GUIDE.md](TESTING_GUIDE.md)
- **Tracking Details**: [COVERAGE_TRACKING.md](COVERAGE_TRACKING.md)

### Run Tests Locally

```bash
# Run all tests
mvn clean test

# Generate coverage report
mvn jacoco:report

# View report
firefox target/site/jacoco/index.html
```

### CI/CD

- ✅ **Automated tests** on every commit
- ✅ **Coverage reports** generated automatically
- ✅ **Release workflow** with coverage history
- ✅ **No blocking** on coverage percentage

**Philosophy**: Track, don't block. Continuous improvement. 📈

## 📈 Performance & Metrics

The MCP Server includes comprehensive performance tracking and tokenization metrics:

### Metrics Endpoints

```bash
# Get all metrics
curl http://localhost:8080/mcp/metrics

# Reset metrics
curl -X POST http://localhost:8080/mcp/metrics/reset
```

### What is Measured

- **Execution Time**: How long each tool takes to process
- **Token Estimation**: Approximate token count (1 token ≈ 4 chars)
- **Cost Estimation**: Estimated LLM API costs (GPT-4 pricing)
- **Cache Performance**: Cache hit rate for each tool
- **Response Size**: Characters and estimated tokens per response

### Documentation

- **Performance Metrics**: [PERFORMANCE_METRICS.md](PERFORMANCE_METRICS.md)
- **Tokenization Guide**: [TOKENIZATION_GUIDE.md](TOKENIZATION_GUIDE.md)
- **Tokenization Architecture**: [TOKENIZATION_ARCHITECTURE.md](TOKENIZATION_ARCHITECTURE.md)

### Example Metrics Response

```json
{
  "tools": {
    "getSchemaStructure": {
      "totalCalls": 150,
      "avgExecutionTimeMs": 45,
      "avgTokens": 3125,
      "totalCostUSD": 0.001875,
      "cacheHitRate": 80.0
    }
  },
  "summary": {
    "totalCalls": 1880,
    "totalCostUSD": 0.3483,
    "averageCostPerCall": 0.00019
  }
}
```

**Note**: Actual tokenization happens in the LLM Host (Claude, GPT-4, etc.), not in the MCP Server. The server provides *estimates* for analysis and optimization.

## License

Open source project.

## Group ID / Artifact ID

- **Group ID**: `com.magacho`
- **Artifact ID**: `aiToSql`

