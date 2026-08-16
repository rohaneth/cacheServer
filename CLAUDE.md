# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Distributed caching system using consistent hashing to distribute cache entries across multiple nodes. The system uses a coordinator-node architecture where:
- **Coordinator**: Manages the hash ring and routes requests to appropriate nodes
- **Nodes**: Store actual cache data and report health status

## Build Commands

```bash
# Compile the project
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ConsistentHashTest

# Run integration tests
mvn test -Dtest=DistributedCacheIntegrationTest

# Clean and build
mvn clean install

# Run the application (coordinator profile)
mvn spring-boot:run -Dspring-boot.run.profiles=coordinator

# Run the application (node profile)
mvn spring-boot:run -Dspring-boot.run.profiles=node

# Package the application
mvn package

# Run with Docker
cd docker && docker-compose up -d
```

## Architecture

### Cache Implementation (`com.cache.distributed.cache`)

- **InMemoryCache**: LRU cache with TTL support using a LinkedHashMap for access-order eviction
- **CacheLoader**: Prevents duplicate requests for the same key using `ConcurrentHashMap` of in-flight requests
- **CacheOperation**: Wrapper enum for operation results (SUCCESS, FAILURE, KEY_NOT_FOUND, KEY_EXPIRED)

### Inter-Service Communication (`com.cache.distributed.config`)

- **RestTemplateConfig**: Configures RestTemplate bean for HTTP communication between coordinator and nodes

### Hash Ring Implementation (`com.cache.distributed.hashring`)

The consistent hash ring is the core routing mechanism:

- **ConsistentHash**: Main ring component that manages physical nodes and their virtual nodes. Uses `ConcurrentSkipListSet` for thread-safe ordered access to virtual nodes by hash value.
- **VirtualNode**: Represents a virtual node in the ring with a hash value, reference to its physical node, and replica index. Implements `Comparable` using unsigned long comparison for proper ordering across the full hash space.
- **HashFunction**: Uses MD5 to generate 32-bit hashes (first 4 bytes of MD5 digest). Provides key hashing and virtual node hash generation (`nodeId-replicaIndex`).

**Key behavior**: When adding a physical node, `virtualNodesPerPhysical` virtual nodes are created and distributed around the ring to ensure even key distribution. When routing a key, the ring finds the first virtual node with hash ≥ key hash (wrapping to first if needed).

### Data Models (`com.cache.distributed.model`)

- **NodeInfo**: Represents a cache node with connection details (host, port), status (ACTIVE/SUSPECTED/FAILED), heartbeat timestamp, and virtual node count. Uses `nodeId` for identity (equals/hashCode).
- **CacheEntry**: Key-value pair with TTL support (createdAt, expiresAt, ttlSeconds). Uses `key` for identity.
- **CacheRequest/CacheResponse**: Request/response wrappers for cache operations
- **HealthStatus**: Node health reporting with current size and timestamp

### Health Monitoring (`com.cache.distributed.health`)

The coordinator maintains cluster health through three components:

- **NodeRegistry**: Thread-safe registry of all nodes using `ConcurrentHashMap`. Tracks nodes by `nodeId` and updates heartbeats.
- **HeartbeatService**: Scheduled task (every `coordinator.health-check-interval-seconds`) that sends health checks to all nodes via `/node/health` endpoint.
- **HealthMonitor**: Scheduled task that removes nodes from the hash ring when their last heartbeat exceeds `coordinator.node-timeout-seconds`. Also re-adds healthy nodes that were temporarily removed.

### Node Registration (`com.cache.distributed.node`)

- **NodeRegistrationService**: Auto-registers nodes with coordinator on startup using `@EventListener(ApplicationReadyEvent.class)`. Sends node info to `http://localhost:8080/coordinator/nodes/register`.
- **NodeConfig**: Configuration properties class for node-specific settings.

### REST API Endpoints

**Coordinator endpoints** (port 8080):
- `POST /coordinator/cache` - Store a key-value pair
- `GET /coordinator/cache/{key}` - Retrieve a value by key
- `DELETE /coordinator/cache/{key}` - Delete a key
- `GET /coordinator/nodes` - List all registered nodes
- `POST /coordinator/nodes/register` - Register a new node

**Node endpoints** (port 8081):
- `POST /node/cache` - Store cache entry (called by coordinator)
- `GET /node/cache/{key}` - Retrieve cache entry
- `DELETE /node/cache/{key}` - Delete cache entry
- `GET /node/health` - Health check endpoint

### Configuration Profiles

The application supports two profiles configured in `application.yml`:

- **coordinator** (default port 8080): Configures virtual node count (default 100), health check interval, and node timeout
- **node** (default port 8081): Configures node ID, memory limits, and TTL

### Application Configuration

- **DistributedCacheApplication**: Main Spring Boot application class with `@EnableScheduling` annotation required for heartbeat/health monitoring scheduled tasks
- **Spring Boot 3.2.0** with Java 17
- **Lombok 1.18.38** for boilerplate reduction
- **Actuator** is included for health monitoring endpoints
- **Tests** use JUnit 5 and are located in `src/test/java/com/cache/distributed/`

## Development Notes

- The hash ring uses unsigned comparison (`Long.compareUnsigned`) for proper ordering across positive hash space
- Virtual nodes per physical is configurable (default 100 in coordinator profile)
- Health monitoring requires `@EnableScheduling` on the main application class
- Nodes auto-register with coordinator on startup via the `NodeRegistrationService`

## Quick Testing

```bash
# Store a value
curl -X POST http://localhost:8080/coordinator/cache \
  -H "Content-Type: application/json" \
  -d '{"key":"user:123","value":"John Doe","ttlSeconds":3600}'

# Retrieve a value
curl http://localhost:8080/coordinator/cache/user:123

# Delete a value
curl -X DELETE http://localhost:8080/coordinator/cache/user:123

# List active nodes
curl http://localhost:8080/coordinator/nodes
```

## Known Limitations

This is a **functional prototype**, not production-ready:

1. **No Data Migration**: When nodes join/leave, affected keys are NOT redistributed to new owners
2. **No Replication**: Data exists on only ONE node - node failure means data loss
3. **No Retry Logic**: Single attempt per request, fail-fast on errors
4. **No Persistence**: All data is in-memory only - restart loses everything
5. **Hardcoded Registration**: Nodes register at `localhost:8080` (no service discovery)
6. **No Security**: Open endpoints, no authentication or encryption
