# Distributed Cache with Consistent Hashing
## B.Tech Semester VII Mid-Term Progress Report

---

**Department:** Electronics and Telecommunication Engineering (EXTC)

**Academic Year:** 2026–2027

**Semester:** VII

**Project Code:** PRJ-EC401

**Project Supervisor:** Dr. Sunil Jatti

**Group Members:**
1. Student Name 1
2. Student Name 2
3. Student Name 3

**Submission Date:** October 15, 2026

---

## 1. Introduction

Distributed caching is a critical component in modern scalable applications, enabling fast data access and reducing load on primary databases. This project implements a distributed caching system using consistent hashing to distribute cache entries across multiple server nodes. The system uses a coordinator-node architecture where the coordinator manages a hash ring and routes client requests to appropriate cache nodes, while individual nodes store the actual data and report their health status.

The primary objective is to demonstrate how consistent hashing minimizes data movement during scaling operations compared to traditional hashing approaches. In traditional hashing, adding or removing a node requires remapping approximately 80% of keys to new locations. With consistent hashing, only about 25% of keys need to be remapped, significantly reducing cache warming overhead and database load during resharding operations.

---

## 2. Problem Statement

As applications scale to handle millions of users and requests, single-server caching solutions become insufficient due to memory limitations and single points of failure. Traditional distributed caching approaches that use simple modulo-based hashing suffer from significant drawbacks:

1. **Excessive Data Movement**: When nodes are added or removed, most cache keys must be redistributed to new nodes, causing sudden spikes in database load and cache warming overhead.

2. **Uneven Distribution**: Simple hashing can lead to uneven key distribution across nodes, causing some nodes to be overloaded while others remain underutilized.

3. **Hotspot Creation**: Certain keys may be frequently accessed, creating hotspots on specific nodes that cannot be balanced effectively.

4. **No Automatic Failover**: When a node fails, there is no automatic mechanism to redistribute its data to healthy nodes.

The project addresses these challenges by implementing a distributed cache with consistent hashing that provides:
- Minimal data remapping during topology changes
- Even key distribution using virtual nodes
- Automatic health monitoring and failover
- Simple REST API interface for all operations

---

## 3. Objectives

### 3.1 Functional Objectives

1. **Implement Consistent Hashing**: Build a hash ring with virtual nodes to distribute cache keys evenly across multiple physical nodes.

2. **Coordinator-Node Architecture**: Design a coordinator that manages the hash ring and routes requests, while nodes store actual cache data.

3. **Health Monitoring**: Implement automatic health checks to detect failed nodes and remove them from the hash ring.

4. **Node Registration**: Enable nodes to automatically register with the coordinator on startup.

5. **Cache Operations**: Provide PUT, GET, and DELETE operations through a REST API.

6. **TTL Support**: Implement time-to-live (TTL) functionality for automatic cache entry expiration.

7. **LRU Eviction**: Use least-recently-used eviction policy when cache reaches capacity limits.

8. **Duplicate Request Protection**: Prevent redundant computations for the same key using a CacheLoader mechanism.

### 3.2 Technical Learning Objectives

1. Understand and implement consistent hashing algorithms
2. Design distributed system architecture with coordinator-node pattern
3. Implement RESTful APIs using Spring Boot
4. Work with concurrent data structures for thread-safe operations
5. Implement scheduled tasks for health monitoring
6. Configure and use Spring profiles for different deployment modes
7. Design and implement data models for distributed systems
8. Write unit tests and integration tests

---

## 4. Project Selection Steps

### 4.1 Problem Identification

The need for scalable caching solutions is evident in modern web applications, social media platforms, e-commerce systems, and content delivery networks. Existing distributed cache systems like Redis Cluster, Memcached, and Hazelcast are mature but complex to understand from first principles. Building a simplified implementation provides valuable learning opportunities in distributed systems design.

### 4.2 Feasibility Analysis

**Technical Feasibility:**
- Java 17 and Spring Boot 3.2.0 provide modern, well-documented frameworks
- Consistent hashing is a well-studied algorithm with clear implementation guidelines
- REST API design is straightforward with Spring Boot's controller architecture
- Docker support enables easy local testing with multiple nodes

**Resource Feasibility:**
- Standard laptop hardware (8GB RAM, modern CPU) is sufficient for development
- Maven for build management is lightweight and fast
- No external paid services or cloud infrastructure required for development
- Open-source libraries (Spring Boot, Lombok, JUnit) are freely available

**Time Feasibility:**
- Mid-term milestone focuses on core architecture and basic functionality
- Advanced features (replication, persistence, security) deferred to final phase
- Incremental development approach allows testing at each stage

### 4.3 Scope Definition

**In Scope (Mid-Term):**
- Consistent hash ring implementation with virtual nodes
- Coordinator for request routing
- Multiple cache nodes with in-memory storage
- Health monitoring and automatic failover
- Basic CRUD operations (PUT, GET, DELETE)
- TTL and LRU eviction
- REST API endpoints
- Unit tests for core components

**Out of Scope (Deferred to Final Phase):**
- Data replication across multiple nodes
- Automatic data migration on topology changes
- Persistent storage backend (Redis, Cassandra)
- Request retry logic with exponential backoff
- Service discovery (Consul, Eureka)
- Authentication and encryption (TLS)
- Metrics and observability (Prometheus, Grafana)

### 4.4 Technology Selection

**Backend Framework:** Spring Boot 3.2.0
- Rapid application development with auto-configuration
- Built-in web server (Tomcat)
- Comprehensive ecosystem for REST APIs, scheduling, and configuration
- Strong community support and documentation

**Language:** Java 17
- Object-oriented with strong typing
- Excellent concurrency support
- Long-term support release
- Industry-standard for enterprise applications

**Build Tool:** Maven
- Standard dependency management
- Clear project structure
- Integration with Spring Boot plugin

**Additional Libraries:**
- Lombok 1.18.38 for boilerplate reduction
- Spring Actuator for health monitoring
- JUnit 5 for testing

**Deployment:** Docker (optional)
- Easy containerization of coordinator and nodes
- Simplified local testing with multiple instances

---

## 5. System Architecture and Workflow

### 5.1 Architecture Overview

The system follows a coordinator-node architecture:

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

### 5.2 Component Architecture

**Coordinator Components:**
- **ConsistentHash**: Manages the hash ring with virtual nodes using `ConcurrentSkipListSet` for thread-safe ordered access
- **NodeRegistry**: Thread-safe registry of all nodes using `ConcurrentHashMap`, tracks nodes by nodeId and updates heartbeats
- **HeartbeatService**: Scheduled task that sends health checks to all nodes via `/node/health` endpoint
- **HealthMonitor**: Scheduled task that removes nodes from hash ring when heartbeat timeout is exceeded, and re-adds recovered nodes
- **CoordinatorService**: Routing logic that determines which node should handle a given key
- **CoordinatorController**: REST endpoints for client requests

**Node Components:**
- **InMemoryCache**: LRU cache with TTL support using LinkedHashMap for access-order eviction
- **CacheLoader**: Prevents duplicate requests for the same key using ConcurrentHashMap of in-flight requests
- **CacheNodeService**: Business logic for cache operations
- **CacheNodeController**: REST endpoints for coordinator communication
- **NodeRegistrationService**: Auto-registers with coordinator on startup
- **NodeConfig**: Configuration properties for node-specific settings

**Shared Components:**
- **HashFunction**: Uses MD5 to generate 32-bit hashes (first 4 bytes of MD5 digest)
- **VirtualNode**: Represents a virtual node with hash value, physical node reference, and replica index
- **NodeInfo**: Data model for node metadata (host, port, status, heartbeat)
- **CacheEntry**: Data model for cache entries with TTL support
- **CacheRequest/CacheResponse**: DTOs for API communication
- **HealthStatus**: Node health reporting with current size and timestamp

### 5.3 Data Flow

**PUT Operation (Store Cache Entry):**
1. Client sends POST request to `/coordinator/cache` with key, value, and TTL
2. Coordinator hashes the key using MD5
3. Coordinator finds the responsible virtual node on the hash ring
4. Coordinator forwards request to the corresponding physical node
5. Node stores the entry in its in-memory cache with LRU eviction
6. Node returns success response to coordinator
7. Coordinator returns success response to client

**GET Operation (Retrieve Cache Entry):**
1. Client sends GET request to `/coordinator/cache/{key}`
2. Coordinator hashes the key
3. Coordinator finds the responsible virtual node
4. Coordinator forwards request to the corresponding node
5. Node retrieves the entry from cache, checks if expired
6. Node returns value or KEY_NOT_FOUND error
7. Coordinator returns response to client

**DELETE Operation (Remove Cache Entry):**
1. Client sends DELETE request to `/coordinator/cache/{key}`
2. Coordinator hashes the key
3. Coordinator finds the responsible virtual node
4. Coordinator forwards request to the corresponding node
5. Node removes the entry from cache
6. Node returns success response
7. Coordinator returns success response to client

**Health Monitoring:**
1. HeartbeatService runs every 10 seconds (configurable)
2. Sends GET request to `/node/health` on each registered node
3. Node returns current cache size and timestamp
4. NodeRegistry updates the node's heartbeat timestamp
5. HealthMonitor checks every 10 seconds
6. Removes nodes with heartbeat older than 30 seconds (configurable)
7. Re-adds nodes that recover and send new heartbeats

**Node Registration:**
1. Node starts with configured nodeId and port
2. NodeRegistrationService triggers on `ApplicationReadyEvent`
3. Node sends POST request to `/coordinator/nodes/register`
4. Coordinator adds node to NodeRegistry
5. Coordinator creates virtual nodes (default 100 per physical node)
6. Coordinator adds virtual nodes to hash ring
7. Node is now available for routing

### 5.4 Hash Ring Operation

**Adding a Physical Node:**
1. Coordinator receives node registration
2. Creates `virtualNodesPerPhysical` virtual nodes (default 100)
3. Each virtual node gets a unique hash based on `nodeId-replicaIndex`
4. All virtual nodes are added to `ConcurrentSkipListSet` in hash order
5. Keys that hash to positions after a virtual node are routed to that node

**Routing a Key:**
1. Key is hashed using MD5 (first 4 bytes as 32-bit integer)
2. Hash ring searches for first virtual node with hash ≥ key hash
3. If no such node exists (key hash is highest), wraps to first virtual node
4. The physical node owning that virtual node handles the request

**Removing a Physical Node:**
1. HealthMonitor detects timeout
2. All virtual nodes belonging to that physical node are removed from ring
3. Subsequent requests are routed to next available virtual nodes
4. Data on failed node is lost (no replication in current implementation)

---

## 6. Current Progress (Mid-Term)

### 6.1 Completed Work

**Project Setup and Configuration:**
- Maven project structure created with Spring Boot 3.2.0 parent
- Dependencies configured: Spring Boot Web, Actuator, Lombok, JUnit 5
- Java 17 compatibility verified
- Build commands documented in CLAUDE.md

**Documentation:**
- Comprehensive README.md with features, architecture, and usage examples
- CLAUDE.md with detailed architecture documentation for development guidance
- API endpoint documentation
- Known limitations clearly documented

**Planned Architecture Design:**
- Complete system architecture defined with coordinator-node pattern
- Hash ring algorithm specified with virtual node implementation
- Health monitoring mechanism designed with scheduled tasks
- Data models defined for NodeInfo, CacheEntry, CacheRequest, CacheResponse, HealthStatus
- REST API endpoints specified for both coordinator and node
- Configuration profiles designed (coordinator, node)

**Technology Stack Finalized:**
- Java 17, Spring Boot 3.2.0, Maven, Lombok 1.18.38
- JUnit 5 for testing
- MD5 for hash function
- ConcurrentHashMap and ConcurrentSkipListSet for thread-safe operations
- LinkedHashMap for LRU cache

### 6.2 Work in Progress

**Source Code Implementation:**
The following components are planned but not yet implemented:

**Cache Implementation (`com.cache.distributed.cache`):**
- InMemoryCache: LRU cache with TTL support
- CacheLoader: Duplicate request protection
- CacheOperation: Operation result wrapper enum

**Hash Ring Implementation (`com.cache.distributed.hashring`):**
- ConsistentHash: Main ring component with virtual node management
- VirtualNode: Virtual node representation with Comparable interface
- HashFunction: MD5-based hash generation

**Coordinator Components (`com.cache.distributed.coordinator`):**
- CoordinatorController: REST endpoints for client requests
- CoordinatorService: Routing logic
- CoordinatorConfig: Configuration properties
- RoutingDecision: Routing result data model

**Health Monitoring (`com.cache.distributed.health`):**
- NodeRegistry: Thread-safe node registry
- HeartbeatService: Scheduled health check task
- HealthMonitor: Scheduled node removal task

**Node Components (`com.cache.distributed.node`):**
- CacheNodeController: Node REST endpoints
- CacheNodeService: Node business logic
- NodeRegistrationService: Auto-registration on startup
- NodeConfig: Node configuration properties

**Data Models (`com.cache.distributed.model`):**
- NodeInfo, CacheEntry, CacheRequest, CacheResponse, HealthStatus

**Configuration:**
- application.yml with coordinator and node profiles
- Virtual node count, health check interval, node timeout settings

**Testing:**
- Unit tests for hash ring functionality
- Unit tests for cache operations
- Integration tests for coordinator-node communication

### 6.3 Testing Status

No tests have been executed yet as the source code implementation is in the planning phase. The test framework (JUnit 5) is configured and ready for use once implementation begins.

### 6.4 Known Issues

- JVM memory allocation errors occurred during previous attempts (evidenced by hs_err_pid*.log files), likely due to insufficient heap size configuration
- No source code files currently exist in the repository (only pom.xml, documentation, and log files)

---

## 7. Modules Planned and Their Purpose

| Module | Purpose | Status |
|--------|---------|--------|
| **Hash Ring Module** | Implements consistent hashing with virtual nodes for even key distribution | Planned |
| **Coordinator Module** | Manages hash ring, routes requests, and monitors node health | Planned |
| **Cache Node Module** | Stores cache entries, handles CRUD operations, reports health status | Planned |
| **Health Monitoring Module** | Detects failed nodes, removes from ring, handles recovery | Planned |
| **Node Registration Module** | Auto-registers nodes with coordinator on startup | Planned |
| **Cache Implementation Module** | LRU cache with TTL support and duplicate request protection | Planned |
| **REST API Module** | Provides HTTP endpoints for all cache operations | Planned |
| **Configuration Module** | Manages application settings via Spring profiles | Planned |
| **Data Models Module** | Defines data structures for nodes, cache entries, requests, responses | Planned |
| **Testing Module** | Unit tests and integration tests for verification | Planned |

---

## 8. Challenges and Considerations

### 8.1 Technical Challenges

**Hash Ring Ordering:**
- Virtual nodes must be maintained in sorted order for efficient routing
- Unsigned long comparison required for proper ordering across positive hash space
- Thread-safe access needed for concurrent routing operations

**Concurrency Control:**
- Multiple clients may access the same key simultaneously
- Hash ring modifications (node addition/removal) must not disrupt ongoing requests
- Node registry must handle concurrent registration and health updates

**Health Detection Accuracy:**
- Too aggressive health checks may incorrectly mark healthy nodes as failed
- Too lax health checks may leave failed nodes in the ring too long
- Network delays can cause false failure detection

**Memory Management:**
- In-memory cache requires careful sizing to avoid out-of-memory errors
- LRU eviction must work correctly under concurrent access
- JVM heap size must be configured appropriately

### 8.2 Integration Challenges

**Node-Coordinator Communication:**
- REST calls between coordinator and nodes must handle network failures
- Retry logic needed for transient failures (deferred to final phase)
- Configuration must allow flexible node and coordinator URLs

**Startup Ordering:**
- Coordinator must be running before nodes can register
- Nodes must handle coordinator unavailability gracefully
- Race conditions possible during cluster initialization

### 8.3 Validation Considerations

**Input Validation:**
- Keys and values must be validated for null, empty, or excessively large values
- TTL values must be positive integers
- Node IDs must be unique

**Error Handling:**
- Node failures must not crash the coordinator
- Invalid requests must return appropriate HTTP status codes
- Timeout errors must be distinguishable from other failures

### 8.4 Security Considerations

**Current Limitations:**
- No authentication on API endpoints (noted as limitation)
- No encryption for data in transit
- No authorization for different client types

**Mitigation:**
- For prototype phase, acceptable to run in trusted environment
- For production, TLS and authentication would be required (deferred to final phase)

### 8.5 Testing Considerations

**Unit Testing:**
- Hash ring behavior must be tested with various node configurations
- Cache operations must be tested under concurrent access
- Health monitoring logic must be tested with simulated timeouts

**Integration Testing:**
- Coordinator-node communication must be tested end-to-end
- Multi-node scenarios must be tested (add/remove nodes during operation)
- Edge cases (all nodes failed, no nodes registered) must be tested

---

## 9. Future Work / Next Steps

### 9.1 Immediate Next Steps (Post Mid-Term)

1. **Implement Hash Ring Module:**
   - Create HashFunction class with MD5 implementation
   - Create VirtualNode class with Comparable interface
   - Create ConsistentHash class with ConcurrentSkipListSet
   - Write unit tests for hash generation and routing

2. **Implement Data Models:**
   - Create NodeInfo, CacheEntry, CacheRequest, CacheResponse, HealthStatus classes
   - Add appropriate annotations (Lombok, Jackson serialization)

3. **Implement Cache Implementation:**
   - Create InMemoryCache with LinkedHashMap for LRU
   - Implement TTL checking on access
   - Create CacheLoader for duplicate request protection
   - Create CacheOperation enum
   - Write unit tests for cache operations

4. **Implement Coordinator Module:**
   - Create CoordinatorController with REST endpoints
   - Create CoordinatorService with routing logic
   - Create CoordinatorConfig with properties
   - Write unit tests for routing decisions

5. **Implement Node Module:**
   - Create CacheNodeController with node endpoints
   - Create CacheNodeService with business logic
   - Create NodeRegistrationService with auto-registration
   - Create NodeConfig with properties
   - Write unit tests for node operations

6. **Implement Health Monitoring:**
   - Create NodeRegistry with ConcurrentHashMap
   - Create HeartbeatService with @Scheduled annotation
   - Create HealthMonitor with @Scheduled annotation
   - Write unit tests for health logic

7. **Configuration:**
   - Create application.yml with coordinator and node profiles
   - Configure virtual node count, health check interval, node timeout
   - Test with different profiles

8. **Integration Testing:**
   - Create DistributedCacheIntegrationTest
   - Test full workflow: register nodes, store/retrieve/delete entries
   - Test health monitoring with simulated node failures
   - Test multiple nodes simultaneously

### 9.2 Final Phase Enhancements

1. **Data Replication:**
   - Implement replication factor configuration
   - Store each key on multiple nodes
   - Handle replica consistency

2. **Data Migration:**
   - Detect keys affected by node changes
   - Migrate data to new owners
   - Handle migration failures gracefully

3. **Persistence:**
   - Integrate with Redis or Cassandra
   - Provide persistent storage option
   - Maintain in-memory cache as hot layer

4. **Retry Logic:**
   - Implement exponential backoff for failed requests
   - Add circuit breaker pattern
   - Handle temporary network issues

5. **Service Discovery:**
   - Integrate with Consul or Eureka
   - Remove hardcoded coordinator URL
   - Support dynamic cluster membership

6. **Security:**
   - Add TLS for encrypted communication
   - Implement authentication (JWT, API keys)
   - Add authorization for different operations

7. **Observability:**
   - Integrate with Prometheus for metrics
   - Add distributed tracing
   - Create Grafana dashboards

8. **Performance Optimization:**
   - Benchmark with various virtual node counts
   - Optimize hash function for speed
   - Implement request batching

---

## 10. Expected Outcome

Upon completion of the mid-term implementation, the system will demonstrate:

1. **Functional Consistent Hashing:**
   - Hash ring with configurable virtual nodes per physical node
   - Even distribution of keys across nodes
   - Minimal remapping when nodes are added or removed

2. **Working Coordinator-Node Architecture:**
   - Coordinator running on port 8080
   - Multiple nodes running on ports 8081, 8082, etc.
   - Successful request routing from coordinator to nodes

3. **Health Monitoring:**
   - Automatic detection of failed nodes
   - Removal of failed nodes from hash ring
   - Re-addition of recovered nodes

4. **Cache Operations:**
   - Successful PUT, GET, DELETE operations via REST API
   - TTL-based expiration
   - LRU eviction when cache is full

5. **Node Registration:**
   - Automatic registration on node startup
   - Dynamic addition of new nodes to cluster

6. **Testing Coverage:**
   - Unit tests for all major components
   - Integration tests for end-to-end workflows
   - Test execution via Maven

The final deliverable will be a working prototype that can be demonstrated via:
- REST API calls using curl or Postman
- Docker Compose for multi-node deployment
- Clear documentation of architecture and usage

---

## 11. Conclusion

This project addresses the critical need for scalable caching solutions in modern distributed applications. By implementing a distributed cache with consistent hashing, the system demonstrates how to minimize data movement during scaling operations, a key advantage over traditional hashing approaches.

The mid-term phase has established:
- Clear project scope and objectives
- Comprehensive system architecture design
- Well-defined technology stack
- Detailed component specifications
- Documentation for development guidance

The planned implementation follows industry best practices for distributed systems design, including thread-safe data structures, scheduled health monitoring, and RESTful API design. The use of Spring Boot 3.2.0 and Java 17 ensures a modern, maintainable codebase.

The current progress shows that the project is on track for successful completion. The architecture design is complete, documentation is thorough, and the implementation roadmap is clear. The next phase will focus on translating the design into working code, followed by comprehensive testing.

The project provides valuable learning opportunities in distributed systems, concurrent programming, REST API design, and modern Java development. The skills acquired through this implementation are directly applicable to real-world software engineering challenges in building scalable, reliable systems.

---

## 12. Signatures

**Project Group Representative:** __________________________

**Date:** October 15, 2026

**Project Supervisor:** __________________________

**Dr. Sunil Jatti**

**Date:** __________________________

**Head of Department:** __________________________

**Department of Electronics and Telecommunication Engineering**

**Date:** __________________________

---

*This report reflects the planned implementation as of the mid-term milestone. Actual implementation progress may vary based on development challenges encountered.*
