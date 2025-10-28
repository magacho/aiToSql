#!/bin/bash

# MCP Server Test Script
# Tests all available tools via JSON-RPC 2.0

BASE_URL="http://localhost:8080/mcp"

echo "=========================================="
echo "MCP Server Test Suite"
echo "=========================================="
echo ""

# Test 1: Server Health Check
echo "1. Testing server health..."
curl -s http://localhost:8080/mcp | jq .
echo ""
echo ""

# Test 2: Initialize MCP Session
echo "2. Initializing MCP session..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "params": {},
    "id": 1
  }' | jq .
echo ""
echo ""

# Test 3: List Available Tools
echo "3. Listing available tools..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "params": {},
    "id": 2
  }' | jq .
echo ""
echo ""

# Test 4: Get Schema Structure
echo "4. Getting schema structure..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "getSchemaStructure",
      "arguments": {}
    },
    "id": 3
  }' | jq .
echo ""
echo ""

# Test 5: Get Table Details (replace 'customers' with your table name)
echo "5. Getting table details for 'customers'..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "getTableDetails",
      "arguments": {
        "tableName": "customers"
      }
    },
    "id": 4
  }' | jq .
echo ""
echo ""

# Test 6: List Triggers (replace 'customers' with your table name)
echo "6. Listing triggers for 'customers'..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "listTriggers",
      "arguments": {
        "tableName": "customers"
      }
    },
    "id": 5
  }' | jq .
echo ""
echo ""

# Test 7: Execute Secure Query
echo "7. Executing secure database query..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "secureDatabaseQuery",
      "arguments": {
        "queryDescription": "SELECT COUNT(*) as total FROM customers",
        "maxRows": 10
      }
    },
    "id": 6
  }' | jq .
echo ""
echo ""

# Test 8: Security Test - Try to execute non-SELECT query
echo "8. Security test - attempting UPDATE query (should fail)..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "secureDatabaseQuery",
      "arguments": {
        "queryDescription": "UPDATE customers SET name = 'test'",
        "maxRows": 10
      }
    },
    "id": 7
  }' | jq .
echo ""
echo ""

# Test 9: Ping
echo "9. Pinging server..."
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "ping",
    "params": {},
    "id": 8
  }' | jq .
echo ""
echo ""

echo "=========================================="
echo "Test suite completed!"
echo "=========================================="
