# 🎯 Automatic JDBC Driver Detection

## Overview

The aiToSql MCP Server now features **automatic JDBC driver detection**, eliminating the need for users to manually specify the driver class name when configuring database connections.

## How It Works

The system uses two strategies to determine the appropriate JDBC driver:

### Strategy 1: Explicit Database Type (Optional)
If you provide the `DB_TYPE` environment variable, the system directly maps it to the correct driver:

```bash
DB_TYPE=postgresql  → org.postgresql.Driver
DB_TYPE=mysql       → com.mysql.cj.jdbc.Driver
DB_TYPE=sqlserver   → com.microsoft.sqlserver.jdbc.SQLServerDriver
DB_TYPE=oracle      → oracle.jdbc.OracleDriver
```

### Strategy 2: URL Pattern Matching (Automatic)
If `DB_TYPE` is not provided (or is invalid), the system automatically detects the driver by analyzing the JDBC URL pattern:

```bash
jdbc:postgresql://... → org.postgresql.Driver
jdbc:mysql://...      → com.mysql.cj.jdbc.Driver
jdbc:sqlserver://...  → com.microsoft.sqlserver.jdbc.SQLServerDriver
jdbc:oracle:...       → oracle.jdbc.OracleDriver
jdbc:h2:...           → org.h2.Driver
```

## Benefits

✅ **Simplified Configuration**: No need to remember or specify driver class names  
✅ **Reduced Errors**: Eliminates typos in driver class configuration  
✅ **Better UX**: Users only need to provide the JDBC URL  
✅ **Intelligent Fallback**: Tries explicit type first, then URL pattern matching  
✅ **Fully Backward Compatible**: Existing configurations with explicit drivers still work

## Usage Examples

### Example 1: No DB_TYPE Specified (Automatic Detection)

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:postgresql://localhost:5432/mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_pass" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest
```

**Result**: Driver automatically detected as `org.postgresql.Driver` ✅

### Example 2: With DB_TYPE (Explicit)

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:mysql://localhost:3306/mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_pass" \
  -e DB_TYPE="mysql" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest
```

**Result**: Driver resolved by type: `com.mysql.cj.jdbc.Driver` ✅

### Example 3: Conflicting Type and URL (Type Takes Priority)

```bash
docker run -d \
  --name aitosql-mcp \
  -e DB_URL="jdbc:mysql://localhost:3306/mydb" \
  -e DB_USERNAME="readonly_user" \
  -e DB_PASSWORD="secure_pass" \
  -e DB_TYPE="postgresql" \
  -p 8080:8080 \
  flaviomagacho/aitosql:latest
```

**Result**: Driver resolved by explicit type: `org.postgresql.Driver` ⚠️  
**Note**: The explicit type takes priority, even if it conflicts with the URL pattern.

## Supported Databases

| Database Type | Aliases | Driver Class |
|--------------|---------|--------------|
| PostgreSQL | `postgresql` | `org.postgresql.Driver` |
| MySQL | `mysql` | `com.mysql.cj.jdbc.Driver` |
| SQL Server | `sqlserver`, `mssql` | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| Oracle | `oracle` | `oracle.jdbc.OracleDriver` |
| H2 | `h2` | `org.h2.Driver` |

All type values are **case-insensitive** (e.g., `PostgreSQL`, `POSTGRESQL`, `postgresql` all work).

## API Endpoint

You can query the supported databases programmatically:

```bash
curl http://localhost:8080/mcp/supported-databases
```

**Response**:
```json
{
  "supportedDatabases": {
    "postgresql": "org.postgresql.Driver",
    "mysql": "com.mysql.cj.jdbc.Driver",
    "sqlserver": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "mssql": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "oracle": "oracle.jdbc.OracleDriver",
    "h2": "org.h2.Driver"
  },
  "autoDetection": {
    "enabled": true,
    "description": "JDBC driver is automatically detected from DB_URL pattern",
    "override": "Use DB_TYPE environment variable to override auto-detection"
  },
  "examples": {
    "postgresql": "jdbc:postgresql://localhost:5432/mydb",
    "mysql": "jdbc:mysql://localhost:3306/mydb",
    "sqlserver": "jdbc:sqlserver://localhost:1433;databaseName=mydb",
    "oracle": "jdbc:oracle:thin:@localhost:1521:xe",
    "h2": "jdbc:h2:mem:testdb"
  }
}
```

## Implementation Details

The automatic detection is implemented in the `JdbcDriverResolver` component:

```java
@Component
public class JdbcDriverResolver {
    
    // Resolves driver by explicit type or URL pattern
    public Optional<String> resolveDriver(String dbType, String jdbcUrl) {
        // Strategy 1: Try by database type
        Optional<String> driverByType = resolveDriverByType(dbType);
        if (driverByType.isPresent()) {
            return driverByType;
        }
        
        // Strategy 2: Try by URL pattern
        return resolveDriverByUrl(jdbcUrl);
    }
}
```

The resolution happens at application startup in the `docker-entrypoint.sh` script:

```bash
# Auto-detect driver from URL if DB_TYPE is not provided
if [ -z "$DB_TYPE" ]; then
    case "$DB_URL" in
        jdbc:postgresql:*)
            JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=org.postgresql.Driver"
            echo "✅ Driver detected: PostgreSQL"
            ;;
        jdbc:mysql:*)
            JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
            echo "✅ Driver detected: MySQL"
            ;;
        # ... (other databases)
    esac
fi
```

## Migration Guide

### Before (Manual Driver Specification)

```bash
docker run -d \
  -e DB_URL="jdbc:postgresql://localhost:5432/mydb" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="pass" \
  -e DB_DRIVER="org.postgresql.Driver"  # ❌ Required
  flaviomagacho/aitosql:latest
```

### After (Automatic Detection)

```bash
docker run -d \
  -e DB_URL="jdbc:postgresql://localhost:5432/mydb" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="pass" \
  # ✅ DB_DRIVER not needed!
  flaviomagacho/aitosql:latest
```

## Testing

The feature includes comprehensive unit tests covering:

- ✅ Resolution by explicit database type
- ✅ Resolution by URL pattern matching
- ✅ Case-insensitive type resolution
- ✅ Fallback from type to URL
- ✅ Invalid/unknown type handling
- ✅ Invalid/unknown URL handling
- ✅ Database type extraction from URLs

Run tests:
```bash
./mvnw test -Dtest=JdbcDriverResolverTest
```

## Troubleshooting

### Issue: Driver Not Detected

**Symptom**: Application fails to start with driver not found error

**Solution**: Ensure your JDBC URL follows the standard pattern:
- PostgreSQL: `jdbc:postgresql://host:port/database`
- MySQL: `jdbc:mysql://host:port/database`
- SQL Server: `jdbc:sqlserver://host:port;databaseName=database`
- Oracle: `jdbc:oracle:thin:@host:port:sid`

### Issue: Wrong Driver Detected

**Symptom**: Application starts but fails to connect

**Solution**: Explicitly specify the database type:
```bash
-e DB_TYPE="postgresql"
```

## Future Enhancements

Planned improvements:

- [ ] Add support for MariaDB
- [ ] Add support for DB2
- [ ] Add support for Sybase
- [ ] Detect driver from database connection metadata
- [ ] Auto-download missing JDBC drivers

## Related Documentation

- [Docker Deployment Guide](DOCKER_DEPLOYMENT.md)
- [Docker README](DOCKER_README.md)
- [Configuration Guide](README.md#configuration)

---

**Feature Version**: 0.3.0+  
**Last Updated**: October 29, 2024
