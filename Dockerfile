# ============================================================================
# Multi-Stage Dockerfile for aiToSql MCP Server
# ============================================================================
# This Dockerfile creates an optimized container image for the MCP Server
# using a multi-stage build to minimize the final image size.
#
# Stage 1: Build the application with Maven
# Stage 2: Create runtime image with JRE only
# ============================================================================

# ============================================================================
# STAGE 1: BUILD
# ============================================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -B

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy entrypoint script (as root before switching user)
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Change ownership of app directory
RUN chown -R spring:spring /app

USER spring:spring

# Expose port (default 8080, can be overridden with SERVER_PORT env var)
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Environment variables with default values
ENV SERVER_PORT=8080 \
    CACHE_ENABLED=true \
    SPRING_PROFILES_ACTIVE=docker \
    DB_URL="" \
    DB_USERNAME="" \
    DB_PASSWORD="" \
    DB_TYPE=""

# Run the application using entrypoint script
ENTRYPOINT ["docker-entrypoint.sh"]

# ============================================================================
# USAGE EXAMPLES
# ============================================================================
#
# Build:
#   docker build -t magacho/aitosql-mcp-server:latest .
#
# Run with PostgreSQL:
#   docker run -d \
#     -e DB_URL="jdbc:postgresql://postgres:5432/mydb" \
#     -e DB_USERNAME="readonly_user" \
#     -e DB_PASSWORD="secure_password" \
#     -e DB_TYPE="PostgreSQL" \
#     -p 8080:8080 \
#     magacho/aitosql-mcp-server:latest
#
# Run with MySQL:
#   docker run -d \
#     -e DB_URL="jdbc:mysql://mysql:3306/mydb" \
#     -e DB_USERNAME="readonly_user" \
#     -e DB_PASSWORD="secure_password" \
#     -e DB_TYPE="MySQL" \
#     -p 8080:8080 \
#     magacho/aitosql-mcp-server:latest
#
# ============================================================================
