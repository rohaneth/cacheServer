# Distributed Cache with Consistent Hashing

A distributed caching system built with Java Spring Boot that uses consistent hashing to distribute data across multiple server nodes, minimizing data movement during scaling operations.

## Features

- **Consistent Hashing**: Even data distribution with minimal remapping on topology changes
- **Automatic Node Management**: Health monitoring with automatic failover
- **REST API**: Simple HTTP interface for all operations
- **In-Memory Storage**: Fast LRU cache with TTL support
- **Duplicate Request Protection**: Prevents redundant computations via CacheLoader
- **Docker Support**: Easy local deployment with multiple nodes
- **Auto-Registration**: Nodes automatically register with coordinator on startup

## Technology Stack

- **Java 17** - Language runtime
- **Spring Boot 3.2.0** - Application framework
- **Maven** - Build tool
- **Lombok 1.18.38** - Boilerplate reduction
- **Spring Actuator** - Health monitoring endpoints
- **JUnit 5** - Testing framework

## Known Limitations

> This is a **functional prototype**, not production-ready. Be aware of these limitations:

1. **No Data Migration**: When nodes join/leave, affected keys are NOT redistributed to new owners
2. **No Replication**: Data exists on only ONE node - node failure means data loss
3. **No Retry Logic**: Single attempt per request, fail-fast on errors
4. **No Persistence**: All data is in-memory only - restart loses everything
5. **Hardcoded Registration**: Nodes register at `localhost:8080` (no service discovery)
6. **No Security**: Open endpoints, no authentication or encryption

## Architecture

```
┌─────────────┐
│  Clients    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Coordinator    │ ──► Health Monitor
│  (Hash Ring)    │
└────────┬────────┘
         │
    ┌────┴────┬────────┐
    ▼         ▼        ▼
┌──────┐ ┌──────┐ ┌──────┐
│Node1 │ │Node2 │ │Node3 │
└──────┘ └──────┘ └──────┘
```

## Quick Start

### Prerequisites
- Java 17+
- Maven
- Docker (optional)

### Build

```bash
mvn clean package
```

### Run Locally (Multiple Terminals)

```bash
# Terminal 1 - Coordinator
mvn spring-boot:run -Dspring-boot.run.profiles=coordinator

# Terminal 2 - Node 1
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-1 -DNODE_PORT=8081

# Terminal 3 - Node 2
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-2 -DNODE_PORT=8082
```

### Run with Docker

```bash
cd docker
docker-compose up -d
```

## Usage Examples

### Store a value

```bash
curl -X POST http://localhost:8080/coordinator/cache \
  -H "Content-Type: application/json" \
  -d '{"key":"user:123","value":"John Doe","ttlSeconds":3600}'
```

### Retrieve a value

```bash
curl http://localhost:8080/coordinator/cache/user:123
```

### Delete a value

```bash
curl -X DELETE http://localhost:8080/coordinator/cache/user:123
```

### List active nodes

```bash
curl http://localhost:8080/coordinator/nodes
```

## API Endpoints

### Coordinator Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/coordinator/cache` | Store a value |
| GET | `/coordinator/cache/{key}` | Retrieve a value |
| DELETE | `/coordinator/cache/{key}` | Delete a value |
| GET | `/coordinator/nodes` | List all nodes |

### Node Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/node/cache` | Store a value (direct) |
| GET | `/node/cache/{key}` | Retrieve a value (direct) |
| DELETE | `/node/cache/{key}` | Delete a value (direct) |
| GET | `/node/health` | Health check |

## Configuration

### Node Settings
```yaml
node:
  id: node-1              # Unique node identifier
  max-memory-size: 1000   # Maximum cache entries
  ttl-seconds: 3600       # Default TTL
```

### Coordinator Settings
```yaml
coordinator:
  virtual-nodes: 100              # Virtual nodes per physical node
  health-check-interval-seconds: 10  # Health check frequency
  node-timeout-seconds: 30        # Node failure threshold
```

## How It Works

1. **Consistent Hashing**: Uses a hash ring with virtual nodes to distribute keys evenly
2. **Routing**: Coordinator routes each request to the appropriate node based on key hash
3. **Health Monitoring**: Automatic detection and removal of failed nodes
4. **Recovery**: Failed nodes are automatically re-added when they recover

## Benefits of Consistent Hashing

When a node is added or removed:
- **Traditional hashing**: ~80% of keys must be remapped
- **Consistent hashing**: ~25% of keys must be remapped

This dramatically reduces:
- Cache warming overhead
- Database load during resharding
- System disruption during scaling

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=InMemoryCacheTest

# Run integration tests
mvn test -Dtest=DistributedCacheIntegrationTest
```

## Troubleshooting

### Out of Memory Errors

If you encounter `hs_err_pid*.log` files with native memory allocation failures:

```bash
# Increase JVM heap size
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2G -Xms512m"

# Or in docker-compose.yml
environment:
  - JAVA_OPTS=-Xmx1G
```

### Port Already in Use

```bash
# Find process using the port (Windows)
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <pid> /F
```

### Node Not Registering with Coordinator

- Verify coordinator is running on port 8080
- Check `NodeConfig` for correct coordinator URL
- Review application logs for registration errors

### Health Check Failures

Nodes are marked as failed after `coordinator.node-timeout-seconds` (default: 30s). Adjust if needed:

```yaml
coordinator:
  node-timeout-seconds: 60  # Increase timeout
```

## Performance Considerations

- **Virtual Nodes**: Default 100 per physical node. Increase for better distribution, decrease for lower overhead.
- **LRU Eviction**: Configure `max-memory-size` per node to limit memory usage.
- **TTL**: Set appropriate `ttlSeconds` to prevent unbounded growth.
- **Health Check Interval**: Lower frequency reduces network overhead but increases failure detection time.

## Future Enhancements

Potential improvements for production readiness:

- [ ] Data replication across multiple nodes
- [ ] Automatic data migration on topology changes
- [ ] Persistent storage backend (Redis, Cassandra)
- [ ] Request retry logic with exponential backoff
- [ ] Service discovery (Consul, Eureka)
- [ ] Authentication and encryption (TLS)
- [ ] Metrics and observability (Prometheus, Grafana)
- [ ] Request batching and pipelining

## Project Structure

```
src/main/java/com/cache/distributed/
├── DistributedCacheApplication.java  # Main entry point
├── cache/
│   ├── InMemoryCache.java            # LRU cache with TTL
│   ├── CacheLoader.java              # Duplicate request protection
│   └── CacheOperation.java          # Operation result wrapper
├── coordinator/
│   ├── CoordinatorController.java   # Coordinator REST endpoints
│   ├── CoordinatorService.java      # Routing logic
│   ├── CoordinatorConfig.java       # Configuration
│   └── RoutingDecision.java         # Routing result
├── health/
│   ├── HealthMonitor.java           # Node health checker
│   ├── NodeRegistry.java            # Node registration
│   └── HeartbeatService.java        # Heartbeat sender
├── hashring/
│   ├── ConsistentHash.java          # Hash ring implementation
│   ├── VirtualNode.java             # Virtual node representation
│   └── HashFunction.java            # Hash function
├── model/
│   ├── CacheEntry.java              # Cache entry model
│   ├── NodeInfo.java                # Node metadata
│   ├── CacheRequest.java            # Request DTO
│   ├── CacheResponse.java           # Response DTO
│   └── HealthStatus.java            # Health status model
└── node/
    ├── CacheNodeController.java     # Node REST endpoints
    ├── CacheNodeService.java        # Node business logic
    └── NodeConfig.java              # Node configuration
```

## License

MIT License
