# 🤖 aiToSql - MCP Server for Database Intelligence

[![Docker Pulls](https://img.shields.io/docker/pulls/flaviomagacho/aitosql)](https://hub.docker.com/r/flaviomagacho/aitosql)
[![Docker Image Size](https://img.shields.io/docker/image-size/flaviomagacho/aitosql/latest)](https://hub.docker.com/r/flaviomagacho/aitosql)
[![GitHub](https://img.shields.io/badge/GitHub-magacho%2FaiToSql-blue)](https://github.com/magacho/aiToSql)

**aiToSql** is a Model Context Protocol (MCP) server that enables AI agents and LLMs to interact intelligently with relational databases using JDBC. Transform your database into an AI-accessible knowledge base.

## 🌟 Key Features

- ✅ **Database Agnostic**: Works with PostgreSQL, MySQL, Oracle, SQL Server
- 🎯 **Auto Driver Detection**: No need to specify JDBC driver class
- 🔐 **Security First**: Read-only operations only (SELECT queries)
- 📊 **Schema Introspection**: Automatic model context generation
- 🚀 **MCP Protocol**: Standard interface for AI agents
- 🐳 **Multi-Architecture**: Supports AMD64 and ARM64 (Apple Silicon)
- 🔄 **Zero Configuration**: Smart defaults for quick start

## 🚀 Quick Start

### Simple Run (Auto-Detect Driver)

```bash
docker run -d \
  --name aitosql \
  -e DB_URL="jdbc:postgresql://your-db:5432/yourdb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_password" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest
```

> 🎯 **Smart!** The JDBC driver is automatically detected from your URL pattern.

## 🎨 Supported Databases

| Database | Example URL | Driver (Auto) |
|----------|-------------|---------------|
| **PostgreSQL** | `jdbc:postgresql://host:5432/db` | ✅ Auto-detected |
| **MySQL** | `jdbc:mysql://host:3306/db` | ✅ Auto-detected |
| **SQL Server** | `jdbc:sqlserver://host:1433;databaseName=db` | ✅ Auto-detected |
| **Oracle** | `jdbc:oracle:thin:@host:1521:sid` | ✅ Auto-detected |

All JDBC drivers are **pre-installed** in the image!

## 🔧 Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://db:5432/myapp` |
| `DB_USERNAME` | Database user (READ-ONLY recommended) | `readonly_user` |
| `DB_PASSWORD` | Database password | `secure_password` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_TYPE` | Override driver detection | Auto-detected |
| `SERVER_PORT` | Server port | `8080` |
| `CACHE_ENABLED` | Enable schema caching | `true` |

## 📡 API Endpoints

### Get Schema Context
```bash
GET http://localhost:8080/api/mcp/model-context
```
Returns the complete database schema structure for AI context.

### Execute Safe Query
```bash
POST http://localhost:8080/api/mcp/execute-search
Content-Type: application/json

{
  "sql": "SELECT * FROM users WHERE active = true LIMIT 10"
}
```

### Get Table Details
```bash
GET http://localhost:8080/api/mcp/table-details?tableName=users
```

### List Triggers
```bash
GET http://localhost:8080/api/mcp/triggers?tableName=orders
```

### Supported Databases Info
```bash
GET http://localhost:8080/mcp/supported-databases
```

## 🐳 Docker Compose Examples

### PostgreSQL
```yaml
version: '3.8'
services:
  aitosql:
    image: flaviomagacho/aitosql:latest
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/mydb
      DB_USERNAME: readonly_user
      DB_PASSWORD: secure_password
    ports:
      - "8080:8080"
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
```

### MySQL
```yaml
version: '3.8'
services:
  aitosql:
    image: flaviomagacho/aitosql:latest
    environment:
      DB_URL: jdbc:mysql://mysql:3306/mydb?useSSL=false
      DB_USERNAME: readonly_user
      DB_PASSWORD: secure_password
    ports:
      - "8080:8080"
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8
    environment:
      MYSQL_DATABASE: mydb
      MYSQL_ROOT_PASSWORD: rootpass
```

## 🔐 Security Best Practices

1. **Always use a READ-ONLY database user**
   ```sql
   -- PostgreSQL example
   CREATE USER readonly_user WITH PASSWORD 'secure_password';
   GRANT CONNECT ON DATABASE mydb TO readonly_user;
   GRANT USAGE ON SCHEMA public TO readonly_user;
   GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
   ```

2. **Never expose credentials in logs or code**

3. **Use secrets management in production** (Kubernetes secrets, AWS Secrets Manager, etc.)

4. **Limit network access** (use private networks when possible)

## 🏗️ Architecture

```
┌─────────────────┐
│   AI Agent/LLM  │  (Claude, GPT, etc.)
└────────┬────────┘
         │ MCP Protocol
         │ (HTTP/JSON-RPC)
         ▼
┌─────────────────┐
│ aiToSql Server  │  (This container)
│ ┌─────────────┐ │
│ │ Auto Driver │ │  ← Detects JDBC driver
│ │  Detection  │ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │   Schema    │ │  ← Introspects DB
│ │ Introspector│ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │ SQL Executor│ │  ← Runs safe queries
│ └─────────────┘ │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│ Your Database   │  (PostgreSQL, MySQL, etc.)
└─────────────────┘
```

## 📊 Performance

- **Startup Time**: ~5-10 seconds
- **Memory**: ~256MB-512MB (adjustable via JVM options)
- **CPU**: Minimum 0.5 cores
- **Schema Caching**: Enabled by default for better performance

## 🎯 Use Cases

1. **AI-Powered Analytics**: Let LLMs query your database naturally
2. **Data Discovery**: AI agents can explore your schema automatically
3. **Automated Reporting**: Generate insights from database data
4. **Database Documentation**: Auto-generate schema documentation
5. **Development Tools**: Integrate with AI coding assistants

## 🔗 Links

- 📦 **GitHub Repository**: https://github.com/magacho/aiToSql
- 📚 **Full Documentation**: https://github.com/magacho/aiToSql/blob/main/README.md
- 🐛 **Report Issues**: https://github.com/magacho/aiToSql/issues
- 💬 **Discussions**: https://github.com/magacho/aiToSql/discussions

## 📜 License

This project is open source. See the LICENSE file in the repository for details.

## 🤝 Contributing

Contributions are welcome! Please visit the GitHub repository to:
- Report bugs
- Suggest new features
- Submit pull requests
- Improve documentation

---

**Maintained by**: Flavio Magacho ([@magacho](https://github.com/magacho))

**Latest Version**: Check [GitHub Releases](https://github.com/magacho/aiToSql/releases) for the latest version and changelog.
