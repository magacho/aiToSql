# MCP Server - Quick Start Guide

## What is this?

This is a **Model Context Protocol (MCP) Server** that allows Large Language Models (LLMs) to interact with databases in a standardized way using JSON-RPC 2.0.

## Project Structure

```
aiToSql (com.magacho)
├── 16 Java source files
├── JSON-RPC 2.0 transport layer
├── 4 MCP Tools for database interaction
└── Multi-database support (Oracle, MySQL, PostgreSQL, MSSQL)
```

## Quick Setup (5 steps)

### 1. Add Database Driver to pom.xml

Uncomment your database driver:

```xml
<!-- For PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Configure Database in application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=readonly_user
spring.datasource.password=password
```

⚠️ **CRITICAL**: User must have READ-ONLY permissions!

### 3. Build

```bash
mvn clean install
```

### 4. Run

```bash
mvn spring-boot:run
```

### 5. Test

```bash
curl http://localhost:8080/mcp
```

## Available MCP Tools

| Tool | Purpose | Parameters |
|------|---------|------------|
| `getSchemaStructure` | Get all tables and columns | `databaseName` (optional) |
| `getTableDetails` | Get table indexes, FKs, constraints | `tableName` (required) |
| `listTriggers` | List triggers for a table | `tableName` (required) |
| `secureDatabaseQuery` | Execute SELECT query | `queryDescription`, `maxRows` |

## Example Usage

### List Available Tools

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "id": 1
  }'
```

### Get Database Schema

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "getSchemaStructure",
      "arguments": {}
    },
    "id": 2
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
        "queryDescription": "SELECT * FROM customers LIMIT 10",
        "maxRows": 10
      }
    },
    "id": 3
  }'
```

## Security Features

✅ READ-ONLY database user (primary defense)
✅ SELECT-only query validation
✅ Dangerous keyword filtering (DROP, DELETE, UPDATE, INSERT)
✅ Query result limits (configurable, default 1000)
✅ Comprehensive logging for audit

## Architecture Highlights

- **No JPA/Hibernate**: Pure JDBC with JdbcTemplate
- **Caching**: Schema metadata cached for performance
- **JSON-RPC 2.0**: Standard transport protocol
- **Database Agnostic**: Works with Oracle, MySQL, PostgreSQL, MSSQL
- **Spring Boot 3.2.1**: Modern Spring framework
- **Java 21**: Latest LTS Java version

## How LLMs Use This

1. **LLM connects** to MCP Server via JSON-RPC 2.0
2. **Discovers available tools** using `tools/list`
3. **Understands database schema** using `getSchemaStructure`
4. **Executes queries** using `secureDatabaseQuery`
5. **Processes results** and provides insights to users

## Benefits

- **Dynamic Knowledge**: LLM accesses real-time data, not just training data
- **Standardization**: Common protocol for any LLM to connect
- **Security**: Multiple layers of SQL injection prevention
- **Hallucination Reduction**: LLM uses actual database data
- **Multi-Database**: Single interface for different databases

## Configuration Options

Edit `application.properties`:

```properties
# Maximum rows returned by queries
mcp.max-query-rows=1000

# Enable/disable query logging
mcp.enable-query-logging=true

# Cache expiration
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=30m
```

## Production Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/aiToSql-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Cloud Run / GKE

The server is stateless and can be deployed to:
- Google Cloud Run
- Google Kubernetes Engine (GKE)
- AWS ECS/Fargate
- Azure Container Instances

Connect to managed databases:
- Google Cloud SQL (PostgreSQL, MySQL, SQL Server)
- AWS RDS
- Azure Database

## Troubleshooting

### Connection Failed
- Check database URL and credentials
- Ensure database is running
- Verify network connectivity

### Permission Denied
- Ensure user has SELECT permissions on target tables
- Check database firewall rules

### Tool Not Found
- Verify JSON-RPC method name is correct
- Check logs for detailed error messages

## Support

For issues or questions, check:
- Application logs: `logging.level.com.magacho.aiToSql=DEBUG`
- JDBC logs: `logging.level.org.springframework.jdbc=DEBUG`

## License

Open source project - Group ID: com.magacho, Artifact ID: aiToSql
