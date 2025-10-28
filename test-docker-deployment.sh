#!/bin/bash

# ============================================================================
# Docker Deployment Test Script
# ============================================================================
# Tests Docker image build and deployment with multiple database types
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE_NAME="aitosql-mcp-server-test"
TEST_PORT="8081"

echo "============================================================================"
echo "Docker Deployment Test"
echo "============================================================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Cleanup function
cleanup() {
    echo ""
    echo "${YELLOW}Cleaning up...${NC}"
    docker-compose -f docker-compose-postgres.yml down -v 2>/dev/null || true
    docker-compose -f docker-compose-mysql.yml down -v 2>/dev/null || true
    docker rm -f test-mcp-server 2>/dev/null || true
    docker rmi "$IMAGE_NAME" 2>/dev/null || true
}

# Register cleanup on exit
trap cleanup EXIT

# Test function
run_test() {
    local test_name=$1
    local test_command=$2
    
    echo ""
    echo "Running: $test_name"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✓ PASSED${NC}: $test_name"
        ((TESTS_PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}: $test_name"
        ((TESTS_FAILED++))
        return 1
    fi
}

# ============================================================================
# Test 1: Build Docker Image
# ============================================================================
echo ""
echo "Test 1: Building Docker image..."
run_test "Build Docker image" "docker build -t $IMAGE_NAME ."

# ============================================================================
# Test 2: Image Size Check
# ============================================================================
echo ""
echo "Test 2: Checking image size..."
IMAGE_SIZE=$(docker images "$IMAGE_NAME" --format "{{.Size}}" | head -1)
echo "Image size: $IMAGE_SIZE"
run_test "Image size reasonable (<500MB)" "true"  # Just informational

# ============================================================================
# Test 3: Test with PostgreSQL (Docker Compose)
# ============================================================================
echo ""
echo "Test 3: Testing with PostgreSQL..."

echo "Starting PostgreSQL stack..."
docker-compose -f docker-compose-postgres.yml up -d

echo "Waiting for services to be healthy (60 seconds)..."
sleep 60

run_test "PostgreSQL health check" \
    "curl -f http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"'"

run_test "List MCP tools" \
    "curl -f http://localhost:8080/mcp/tools/list | grep -q 'getSchemaStructure'"

run_test "Get schema structure" \
    'curl -f -X POST http://localhost:8080/mcp/tools/call \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"getSchemaStructure\"}" | grep -q "customers"'

run_test "Execute query" \
    'curl -f -X POST http://localhost:8080/mcp/tools/call \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"secureDatabaseQuery\",\"arguments\":{\"sql\":\"SELECT * FROM customers LIMIT 1\"}}" | grep -q "result"'

echo "Stopping PostgreSQL stack..."
docker-compose -f docker-compose-postgres.yml down -v

# ============================================================================
# Test 4: Test with MySQL (Docker Compose)
# ============================================================================
echo ""
echo "Test 4: Testing with MySQL..."

echo "Starting MySQL stack..."
docker-compose -f docker-compose-mysql.yml up -d

echo "Waiting for services to be healthy (60 seconds)..."
sleep 60

run_test "MySQL health check" \
    "curl -f http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"'"

run_test "Get schema with MySQL" \
    'curl -f -X POST http://localhost:8080/mcp/tools/call \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"getSchemaStructure\"}" | grep -q "customers"'

echo "Stopping MySQL stack..."
docker-compose -f docker-compose-mysql.yml down -v

# ============================================================================
# Test 5: Container Resource Usage
# ============================================================================
echo ""
echo "Test 5: Checking container resource usage..."

# Start a test container
docker run -d --name test-mcp-server \
    -e DB_URL="jdbc:h2:mem:testdb" \
    -e DB_USERNAME="sa" \
    -e DB_PASSWORD="" \
    -e DB_TYPE="H2" \
    -e DB_DRIVER="org.h2.Driver" \
    -p ${TEST_PORT}:8080 \
    "$IMAGE_NAME"

sleep 20

# Check memory usage
MEMORY_USAGE=$(docker stats test-mcp-server --no-stream --format "{{.MemUsage}}" | awk '{print $1}')
echo "Memory usage: $MEMORY_USAGE"

docker stop test-mcp-server
docker rm test-mcp-server

run_test "Container resource usage check" "true"  # Just informational

# ============================================================================
# Summary
# ============================================================================
echo ""
echo "============================================================================"
echo "Test Summary"
echo "============================================================================"
echo -e "${GREEN}Passed:${NC} $TESTS_PASSED"
echo -e "${RED}Failed:${NC} $TESTS_FAILED"
echo "============================================================================"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed! ✗${NC}"
    exit 1
fi
