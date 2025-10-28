"""
Python MCP Client Example
Demonstrates how to interact with the MCP Server using JSON-RPC 2.0
"""

import requests
import json
from typing import Dict, Any, Optional

class McpClient:
    def __init__(self, base_url: str = "http://localhost:8080/mcp"):
        self.base_url = base_url
        self.request_id = 0
    
    def _call(self, method: str, params: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Make a JSON-RPC 2.0 call to the MCP server"""
        self.request_id += 1
        
        payload = {
            "jsonrpc": "2.0",
            "method": method,
            "params": params or {},
            "id": self.request_id
        }
        
        response = requests.post(
            self.base_url,
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        response.raise_for_status()
        result = response.json()
        
        if "error" in result:
            raise Exception(f"MCP Error: {result['error']}")
        
        return result.get("result", {})
    
    def initialize(self) -> Dict[str, Any]:
        """Initialize MCP session"""
        return self._call("initialize")
    
    def list_tools(self) -> Dict[str, Any]:
        """List available MCP tools"""
        return self._call("tools/list")
    
    def get_schema_structure(self, database_name: Optional[str] = None) -> Dict[str, Any]:
        """Get complete database schema structure"""
        params = {
            "name": "getSchemaStructure",
            "arguments": {}
        }
        if database_name:
            params["arguments"]["databaseName"] = database_name
        
        return self._call("tools/call", params)
    
    def get_table_details(self, table_name: str) -> Dict[str, Any]:
        """Get detailed information about a specific table"""
        params = {
            "name": "getTableDetails",
            "arguments": {
                "tableName": table_name
            }
        }
        return self._call("tools/call", params)
    
    def list_triggers(self, table_name: str) -> Dict[str, Any]:
        """List triggers for a specific table"""
        params = {
            "name": "listTriggers",
            "arguments": {
                "tableName": table_name
            }
        }
        return self._call("tools/call", params)
    
    def execute_query(self, query: str, max_rows: Optional[int] = None) -> Dict[str, Any]:
        """Execute a secure SELECT query"""
        params = {
            "name": "secureDatabaseQuery",
            "arguments": {
                "queryDescription": query
            }
        }
        if max_rows:
            params["arguments"]["maxRows"] = max_rows
        
        return self._call("tools/call", params)
    
    def ping(self) -> Dict[str, Any]:
        """Ping the MCP server"""
        return self._call("ping")


def main():
    """Example usage of MCP Client"""
    
    # Initialize client
    client = McpClient("http://localhost:8080/mcp")
    
    print("=" * 60)
    print("MCP Client Example")
    print("=" * 60)
    print()
    
    # Test 1: Initialize session
    print("1. Initializing MCP session...")
    init_response = client.initialize()
    print(f"Protocol Version: {init_response.get('protocolVersion')}")
    print(f"Server: {init_response.get('serverInfo')}")
    print()
    
    # Test 2: List available tools
    print("2. Listing available tools...")
    tools = client.list_tools()
    print(f"Available tools: {len(tools.get('tools', []))}")
    for tool in tools.get('tools', []):
        print(f"  - {tool['name']}: {tool['description']}")
    print()
    
    # Test 3: Get schema structure
    print("3. Getting database schema structure...")
    schema = client.get_schema_structure()
    print(json.dumps(schema, indent=2))
    print()
    
    # Test 4: Get table details
    print("4. Getting details for 'customers' table...")
    try:
        table_details = client.get_table_details("customers")
        print(json.dumps(table_details, indent=2))
    except Exception as e:
        print(f"Error: {e}")
    print()
    
    # Test 5: Execute query
    print("5. Executing secure query...")
    try:
        query_result = client.execute_query(
            "SELECT * FROM customers LIMIT 10",
            max_rows=10
        )
        print(json.dumps(query_result, indent=2))
    except Exception as e:
        print(f"Error: {e}")
    print()
    
    # Test 6: Security test (should fail)
    print("6. Security test - attempting UPDATE query...")
    try:
        client.execute_query("UPDATE customers SET name = 'test'")
        print("WARNING: Security validation failed!")
    except Exception as e:
        print(f"Expected error (security working): {e}")
    print()
    
    # Test 7: Ping
    print("7. Pinging server...")
    ping_response = client.ping()
    print(f"Server status: {ping_response.get('status')}")
    print()
    
    print("=" * 60)
    print("All tests completed!")
    print("=" * 60)


if __name__ == "__main__":
    main()
