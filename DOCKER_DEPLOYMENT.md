# 🐳 Docker Deployment Guide

## Overview

This guide explains how to deploy the **aiToSql MCP Server** using Docker. The server is available as a pre-built image on Docker Hub and can be configured entirely through environment variables.

**Docker Hub**: `magacho/aitosql-mcp-server`

---

## 🚀 Quick Start

### Pull the Image

```bash
docker pull magacho/aitosql-mcp-server:latest
```

### Run with PostgreSQL

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:postgresql://your-postgres-host:5432/your_database" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="your_secure_password" \
  -e DB_TYPE="PostgreSQL" \
  -p 8080:8080 \
  magacho/aitosql-mcp-server:latest
```

### Test the Server

```bash
# Health check
curl http://localhost:8080/actuator/health

# List available MCP tools
curl http://localhost:8080/mcp/tools/list

# Get schema structure
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"getSchemaStructure"}'
```

---

## 📋 Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://host:5432/db` |
| `DB_USERNAME` | Database username (READ-ONLY recommended) | `readonly_user` |
| `DB_PASSWORD` | Database password | `secure_password` |
| `DB_TYPE` | Database type (for identification) | `PostgreSQL`, `MySQL`, `Oracle`, `MSSQL` |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_DRIVER` | Auto-detected | JDBC driver class name |
| `SERVER_PORT` | `8080` | HTTP server port |
| `CACHE_ENABLED` | `true` | Enable schema caching |
| `LOGGING_LEVEL_ROOT` | `INFO` | Root logging level |
| `LOGGING_LEVEL_COM_MAGACHO` | `INFO` | Application logging level |

---

## 🗄️ Supported Databases

The Docker image includes drivers for:

- ✅ **PostgreSQL** (all versions)
- ✅ **MySQL** / MariaDB (5.7+, 8.0+)
- ✅ **Microsoft SQL Server** (2017+)
- ⚠️ **Oracle** (requires license, commented out by default)

---

## 🐘 PostgreSQL Example

### Using Docker Run

```bash
docker run -d \
  --name aitosql-postgres \
  -e DB_URL="jdbc:postgresql://postgres.example.com:5432/production_db" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="ReadOnlyPassword123!" \
  -e DB_DRIVER="org.postgresql.Driver" \
  -e DB_TYPE="PostgreSQL" \
  -e SERVER_PORT="8080" \
  -p 8080:8080 \
  --restart unless-stopped \
  magacho/aitosql-mcp-server:latest
```

### Using Docker Compose

```yaml
version: '3.8'

services:
  mcp-server:
    image: magacho/aitosql-mcp-server:latest
    container_name: aitosql-mcp
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/mydb
      DB_USERNAME: readonly_user
      DB_PASSWORD: readonly_password
      DB_TYPE: PostgreSQL
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    restart: unless-stopped
```

---

## 🐬 MySQL Example

### Using Docker Run

```bash
docker run -d \
  --name aitosql-mysql \
  -e DB_URL="jdbc:mysql://mysql.example.com:3306/production_db" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="ReadOnlyPassword123!" \
  -e DB_DRIVER="com.mysql.cj.jdbc.Driver" \
  -e DB_TYPE="MySQL" \
  -e SERVER_PORT="8080" \
  -p 8080:8080 \
  --restart unless-stopped \
  magacho/aitosql-mcp-server:latest
```

### Using Docker Compose

```yaml
version: '3.8'

services:
  mcp-server:
    image: magacho/aitosql-mcp-server:latest
    container_name: aitosql-mcp
    environment:
      DB_URL: jdbc:mysql://mysql:3306/mydb
      DB_USERNAME: readonly_user
      DB_PASSWORD: readonly_password
      DB_TYPE: MySQL
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    restart: unless-stopped
```

---

## 🔷 SQL Server Example

```bash
docker run -d \
  --name aitosql-sqlserver \
  -e DB_URL="jdbc:sqlserver://sqlserver.example.com:1433;databaseName=production_db;encrypt=true;trustServerCertificate=false" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="ReadOnlyPassword123!" \
  -e DB_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver" \
  -e DB_TYPE="MSSQL" \
  -e SERVER_PORT="8080" \
  -p 8080:8080 \
  --restart unless-stopped \
  magacho/aitosql-mcp-server:latest
```

---

## 🧪 Local Development with Docker Compose

The repository includes pre-configured `docker-compose` files for testing:

### PostgreSQL

```bash
docker-compose -f docker-compose-postgres.yml up -d
```

### MySQL

```bash
docker-compose -f docker-compose-mysql.yml up -d
```

These compose files will:
- Start the database with sample data
- Create a `readonly_user` with proper permissions
- Start the MCP Server connected to the database
- Expose port 8080 for API access

---

## 🔒 Security Best Practices

### 1. Use Read-Only Database User

**CRITICAL**: Always use a database user with **SELECT-only** permissions.

#### PostgreSQL

```sql
CREATE USER readonly_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE your_db TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;
```

#### MySQL

```sql
CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT ON your_db.* TO 'readonly_user'@'%';
FLUSH PRIVILEGES;
```

#### SQL Server

```sql
CREATE LOGIN readonly_user WITH PASSWORD = 'secure_password';
USE your_db;
CREATE USER readonly_user FOR LOGIN readonly_user;
ALTER ROLE db_datareader ADD MEMBER readonly_user;
```

### 2. Use Docker Secrets (Production)

For production, use Docker secrets or Kubernetes secrets instead of environment variables:

```bash
docker run -d \
  --name aitosql-mcp \
  --secret db_password \
  -e DB_PASSWORD_FILE=/run/secrets/db_password \
  ... other env vars ... \
  magacho/aitosql-mcp-server:latest
```

### 3. Network Isolation

Place the MCP Server in a restricted network:

```yaml
version: '3.8'

networks:
  backend:
    driver: bridge

services:
  mcp-server:
    image: magacho/aitosql-mcp-server:latest
    networks:
      - backend
    # No exposed ports to host
```

### 4. SSL/TLS for Database Connections

Use encrypted connections to the database:

```bash
# PostgreSQL with SSL
DB_URL="jdbc:postgresql://host:5432/db?ssl=true&sslmode=require"

# MySQL with SSL
DB_URL="jdbc:mysql://host:3306/db?useSSL=true&requireSSL=true"

# SQL Server with encryption
DB_URL="jdbc:sqlserver://host:1433;databaseName=db;encrypt=true"
```

---

## 🐛 Troubleshooting

### Container Won't Start

```bash
# Check logs
docker logs aitosql-mcp

# Common issues:
# - Database connection refused (check DB_URL, host, port)
# - Authentication failed (check DB_USERNAME, DB_PASSWORD)
# - Driver not found (check DB_TYPE and DB_DRIVER)
```

### Health Check Failing

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"}}}
```

### Database Connection Issues

```bash
# Test database connectivity from container
docker exec -it aitosql-mcp sh
curl http://localhost:8080/actuator/health
```

### Performance Issues

```bash
# Increase memory if needed
docker run -d \
  --name aitosql-mcp \
  --memory="512m" \
  --cpus="1.0" \
  ... env vars ... \
  magacho/aitosql-mcp-server:latest
```

---

## 📊 Monitoring

### Health Check Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### Metrics Endpoint

```bash
curl http://localhost:8080/actuator/metrics
```

### Database Connection Status

```bash
curl http://localhost:8080/actuator/health | jq '.components.db'
```

---

## 🔄 Updating the Container

```bash
# Pull latest image
docker pull magacho/aitosql-mcp-server:latest

# Stop and remove old container
docker stop aitosql-mcp
docker rm aitosql-mcp

# Start new container with same configuration
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="..." \
  ... same env vars ... \
  magacho/aitosql-mcp-server:latest
```

---

## 📦 Image Tags

- `latest` - Latest stable release (main branch)
- `0.2.0`, `0.2`, `0` - Semantic versioning
- `develop` - Development branch (unstable)

---

## 🏗️ Building Your Own Image

If you need to customize the image:

```bash
# Clone the repository
git clone https://github.com/magacho/aiToSql.git
cd aiToSql

# Build the image
docker build -t my-custom-mcp-server .

# Run your custom image
docker run -d \
  -e DB_URL="..." \
  ... env vars ... \
  my-custom-mcp-server
```

---

## 🆘 Support

- **GitHub Issues**: https://github.com/magacho/aiToSql/issues
- **Documentation**: https://github.com/magacho/aiToSql
- **Docker Hub**: https://hub.docker.com/r/magacho/aitosql-mcp-server

---

## 📜 License

This project is licensed under the MIT License. See LICENSE file for details.
