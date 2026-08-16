#!/bin/bash

set -e

COORDINATOR_URL="http://localhost:8080"

echo "=== Distributed Cache Demo ==="
echo ""

# Function to store a value
store_value() {
    local key=$1
    local value=$2
    echo "Storing: $key = $value"
    curl -s -X POST "$COORDINATOR_URL/coordinator/cache" \
         -H "Content-Type: application/json" \
         -d "{\"key\":\"$key\",\"value\":\"$value\"}" | jq '.'
    echo ""
}

# Function to retrieve a value
get_value() {
    local key=$1
    echo "Retrieving: $key"
    curl -s -X GET "$COORDINATOR_URL/coordinator/cache/$key" | jq '.'
    echo ""
}

# Function to delete a value
delete_value() {
    local key=$1
    echo "Deleting: $key"
    curl -s -X DELETE "$COORDINATOR_URL/coordinator/cache/$key" | jq '.'
    echo ""
}

# Function to list nodes
list_nodes() {
    echo "Current nodes:"
    curl -s -X GET "$COORDINATOR_URL/coordinator/nodes" | jq '.'
    echo ""
}

echo "Step 1: Start the system first!"
echo "Run: docker-compose -f docker/docker-compose.yml up -d"
echo ""

echo "Step 2: Store some values"
store_value "user:1" "Alice"
store_value "user:2" "Bob"
store_value "user:3" "Charlie"

echo "Step 3: Retrieve values"
get_value "user:1"
get_value "user:2"
get_value "user:3"

echo "Step 4: List nodes"
list_nodes

echo "Demo complete!"
