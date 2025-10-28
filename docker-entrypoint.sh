#!/bin/sh
# ============================================================================
# Docker Entrypoint Script for aiToSql MCP Server
# ============================================================================
# This script configures the application based on environment variables
# and starts the Spring Boot application with the appropriate settings.
# ============================================================================

set -e

# Function to determine JDBC driver class based on DB_TYPE
get_driver_class() {
    case "$1" in
        postgresql|PostgreSQL|POSTGRESQL)
            echo "org.postgresql.Driver"
            ;;
        mysql|MySQL|MYSQL)
            echo "com.mysql.cj.jdbc.Driver"
            ;;
        sqlserver|SQLServer|SQLSERVER|mssql|MSSQL)
            echo "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            ;;
        oracle|Oracle|ORACLE)
            echo "oracle.jdbc.OracleDriver"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Build Java options
JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Dserver.port=${SERVER_PORT:-8080}"

# Configure datasource if DB_URL is provided
if [ -n "$DB_URL" ]; then
    echo "🔧 Configurando conexão com banco de dados..."
    JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.url=$DB_URL"
    
    if [ -n "$DB_USERNAME" ]; then
        JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.username=$DB_USERNAME"
    fi
    
    if [ -n "$DB_PASSWORD" ]; then
        JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.password=$DB_PASSWORD"
    fi
    
    if [ -n "$DB_TYPE" ]; then
        DRIVER_CLASS=$(get_driver_class "$DB_TYPE")
        if [ -n "$DRIVER_CLASS" ]; then
            JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=$DRIVER_CLASS"
            echo "✅ Driver configurado: $DRIVER_CLASS"
        else
            echo "⚠️  Tipo de banco não reconhecido: $DB_TYPE"
        fi
    fi
    
    # Extract database type from URL if not provided
    if [ -z "$DB_TYPE" ]; then
        case "$DB_URL" in
            jdbc:postgresql:*)
                JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=org.postgresql.Driver"
                echo "✅ Driver detectado: PostgreSQL"
                ;;
            jdbc:mysql:*)
                JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
                echo "✅ Driver detectado: MySQL"
                ;;
            jdbc:sqlserver:*)
                JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver"
                echo "✅ Driver detectado: SQL Server"
                ;;
            jdbc:oracle:*)
                JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.driver-class-name=oracle.jdbc.OracleDriver"
                echo "✅ Driver detectado: Oracle"
                ;;
        esac
    fi
else
    echo "⚠️  Nenhuma URL de banco configurada (DB_URL não definido)"
    echo "ℹ️  Aplicação iniciará sem conexão de banco de dados"
fi

# Configure cache
if [ "$CACHE_ENABLED" = "true" ] || [ "$CACHE_ENABLED" = "TRUE" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dspring.cache.type=simple"
else
    JAVA_OPTS="$JAVA_OPTS -Dspring.cache.type=none"
fi

# Configure Spring profile
if [ -n "$SPRING_PROFILES_ACTIVE" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE"
fi

echo "🚀 Iniciando aiToSql MCP Server..."
echo "📋 Java Options: $JAVA_OPTS"

# Execute the application
exec java $JAVA_OPTS -jar app.jar
