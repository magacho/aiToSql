# aiToSql MCP Server

🚀 **Model Context Protocol (MCP) Server for Database Introspection and Query Execution**

A Spring Boot 3 application that provides a standardized MCP interface for LLMs to interact with relational databases. Transform your AI agents into database-aware assistants!

---

## 🎯 Features

- ✅ **MCP JSON-RPC 2.0 Protocol** - Standardized communication for AI agents
- ✅ **Multi-Database Support** - PostgreSQL, MySQL, SQL Server (Oracle ready)
- ✅ **Schema Introspection** - Full database metadata extraction (tables, columns, indexes, foreign keys, triggers)
- ✅ **Secure Query Execution** - Read-only SQL execution with validation
- ✅ **Tokenization Metrics** - Token counting and cost estimation for LLM operations
- ✅ **Caching** - Schema caching for improved performance
- ✅ **Health Checks** - Built-in health monitoring via Spring Boot Actuator
- ✅ **Multi-Architecture** - Supports AMD64 and ARM64

---

## 🚀 Quick Start

### Run with PostgreSQL

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:postgresql://your-host:5432/your_database" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -e DB_TYPE="PostgreSQL" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest
```

### Run with MySQL

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:mysql://your-host:3306/your_database" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -e DB_TYPE="MySQL" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest
```

### Run with SQL Server

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:sqlserver://your-host:1433;databaseName=your_database" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -e DB_TYPE="SQLServer" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest
```

### Test the Server

```bash
# Health check
curl http://localhost:8080/actuator/health

# List MCP tools
curl http://localhost:8080/mcp/tools/list

# Get database schema
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"getSchemaStructure"}'
```

---

## 📋 Environment Variables

### Required

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://host:5432/db` |
| `DB_USERNAME` | Database username (READ-ONLY) | `readonly_user` |
| `DB_PASSWORD` | Database password | `secure_password` |
| `DB_TYPE` | Database type | `PostgreSQL`, `MySQL`, `SQLServer`, `Oracle` |

**Note**: If `DB_TYPE` is not specified, the driver will be auto-detected from the JDBC URL.

### Optional

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP server port |
| `CACHE_ENABLED` | `true` | Enable schema caching |
| `SPRING_PROFILES_ACTIVE` | `docker` | Spring Boot profile |

---

## 🔒 Security

**IMPORTANT**: Always use a **READ-ONLY** database user. This is your primary defense against SQL injection.

### PostgreSQL Example

```sql
CREATE USER readonly_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE your_db TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
```

### MySQL Example

```sql
CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT ON your_db.* TO 'readonly_user'@'%';
FLUSH PRIVILEGES;
```

### SQL Server Example

```sql
CREATE LOGIN readonly_user WITH PASSWORD = 'secure_password';
USE your_database;
CREATE USER readonly_user FOR LOGIN readonly_user;
GRANT SELECT TO readonly_user;
```

---

## 🛠️ MCP Tools Available

1. **getSchemaStructure** - Get complete database schema (tables, columns, types)
2. **getTableDetails** - Get detailed table information (indexes, foreign keys, constraints)
3. **listTriggers** - List triggers for a specific table
4. **secureDatabaseQuery** - Execute safe SELECT queries with tokenization metrics

---

## 🐳 Docker Compose Examples

### PostgreSQL Full Stack

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mcp-server:
    image: magacho/aitosql-mcp-server:latest
    depends_on:
      - postgres
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/mydb
      DB_USERNAME: readonly_user
      DB_PASSWORD: readonly_password
      DB_TYPE: PostgreSQL
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

### MySQL Full Stack

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8-oracle
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: mydb
      MYSQL_USER: readonly_user
      MYSQL_PASSWORD: readonly_password
    volumes:
      - mysql_data:/var/lib/mysql

  mcp-server:
    image: magacho/aitosql-mcp-server:latest
    depends_on:
      - mysql
    environment:
      DB_URL: jdbc:mysql://mysql:3306/mydb
      DB_USERNAME: readonly_user
      DB_PASSWORD: readonly_password
      DB_TYPE: MySQL
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

---

## 📊 Image Information

- **Repository**: `magacho/aitosql-mcp-server`
- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Size**: ~200MB (optimized multi-stage build)
- **Architectures**: `linux/amd64`, `linux/arm64`
- **Included Drivers**: PostgreSQL, MySQL, SQL Server

### Available Tags

- `latest` - Latest stable release
- `X.Y.Z` - Specific version (e.g., `0.2.0`)
- `vX.Y.Z` - Version with 'v' prefix (e.g., `v0.2.0`)

---

## 🔍 Supported Database Types

| Database | JDBC URL Pattern | Driver Class |
|----------|------------------|--------------|
| PostgreSQL | `jdbc:postgresql://host:port/db` | `org.postgresql.Driver` |
| MySQL | `jdbc:mysql://host:port/db` | `com.mysql.cj.jdbc.Driver` |
| SQL Server | `jdbc:sqlserver://host:port;databaseName=db` | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| Oracle | `jdbc:oracle:thin:@host:port:sid` | `oracle.jdbc.OracleDriver` |

---

## 📈 Performance & Monitoring

### Health Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed metrics
curl http://localhost:8080/actuator/metrics
```

### Logs

```bash
# View container logs
docker logs -f aitosql-mcp

# View last 100 lines
docker logs --tail 100 aitosql-mcp
```

---

## 🔗 Links

- **GitHub Repository**: https://github.com/magacho/aiToSql
- **Docker Hub**: https://hub.docker.com/r/magacho/aitosql-mcp-server
- **Full Documentation**: https://github.com/magacho/aiToSql/blob/main/README.md
- **Deployment Guide**: https://github.com/magacho/aiToSql/blob/main/DOCKER_DEPLOYMENT.md
- **Issues**: https://github.com/magacho/aiToSql/issues

---

## 💡 Integration Examples

### Integrate with LLM Host

Point your LLM to the MCP server endpoint:

```json
{
  "mcpServers": {
    "database": {
      "url": "http://localhost:8080/mcp",
      "type": "json-rpc-2.0"
    }
  }
}
```

### Query Example via LLM

The LLM can now ask:
- "Show me the database schema"
- "List all customers from Brazil"
- "What are the total sales by region?"

The MCP server will handle tool calls, execute queries safely, and return structured results with tokenization metrics.

---

## 📜 License

MIT License - see [LICENSE](https://github.com/magacho/aiToSql/blob/main/LICENSE) for details.

---

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/magacho/aiToSql/blob/main/CONTRIBUTING.md) for guidelines.

---

**Made with ❤️ for the AI and Database communities**

**Version**: See [releases](https://github.com/magacho/aiToSql/releases) for latest version  
**Maintained by**: [@magacho](https://github.com/magacho)
