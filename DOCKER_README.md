# aiToSql MCP Server

🚀 **Model Context Protocol (MCP) Server for Database Introspection and Query Execution**

A Spring Boot 3 application that provides a standardized MCP interface for LLMs to interact with relational databases.

---

## 🎯 Features

- ✅ **MCP JSON-RPC 2.0 Protocol** - Standardized communication for AI agents
- ✅ **Multi-Database Support** - PostgreSQL, MySQL, SQL Server
- ✅ **Schema Introspection** - Full database metadata extraction (tables, columns, indexes, foreign keys, triggers)
- ✅ **Secure Query Execution** - Read-only SQL execution with validation
- ✅ **Tokenization Metrics** - Token counting and cost estimation for LLM operations
- ✅ **Caching** - Schema caching for improved performance
- ✅ **Health Checks** - Built-in health monitoring via Spring Boot Actuator

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
| `DB_TYPE` | Database type | `PostgreSQL`, `MySQL`, `MSSQL` |

### Optional

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP server port |
| `CACHE_ENABLED` | `true` | Enable schema caching |
| `LOGGING_LEVEL_ROOT` | `INFO` | Root logging level |

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

---

## 🛠️ MCP Tools Available

1. **getSchemaStructure** - Get complete database schema (tables, columns, types)
2. **getTableDetails** - Get detailed table information (indexes, foreign keys, constraints)
3. **listTriggers** - List triggers for a specific table
4. **secureDatabaseQuery** - Execute safe SELECT queries

---

## 🐳 Docker Compose Example

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres

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
```

---

## 📊 Image Information

- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Size**: ~200MB (optimized multi-stage build)
- **Architectures**: `linux/amd64`, `linux/arm64`
- **Included Drivers**: PostgreSQL, MySQL, SQL Server

---

## 🔗 Links

- **GitHub Repository**: https://github.com/magacho/aiToSql
- **Full Documentation**: https://github.com/magacho/aiToSql/blob/main/DOCKER_DEPLOYMENT.md
- **Issues**: https://github.com/magacho/aiToSql/issues

---

## 📜 License

MIT License - see [LICENSE](https://github.com/magacho/aiToSql/blob/main/LICENSE) for details.

---

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/magacho/aiToSql/blob/main/CONTRIBUTING.md) for guidelines.

---

**Made with ❤️ for the AI and Database communities**
