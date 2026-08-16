# Distributed Cache - Interview Guide & Documentation

## Table of Contents
1. [What This Application Does](#what-this-application-does)
2. [Interview-Ready Explanation](#interview-ready-explanation)
3. [What Was Implemented](#what-was-implemented)
4. [Testing Guide](#testing-guide)
5. [Common Interview Questions](#common-interview-questions)

---

## What This Application Does

### Simple Explanation
This is a **distributed cache system** - like having multiple warehouses to store products instead of one giant store.

**Real-world analogy:**
- **Single cache** = One librarian managing all books
- **Distributed cache** = Multiple librarians, each managing part of the collection, with a coordinator telling you which librarian has your book

### Technical Overview
- **Coordinator** (port 8080): Routes requests to appropriate nodes
- **Nodes** (ports 8081+): Store actual cache data
- **Consistent Hashing**: Ensures even distribution and minimal remapping when nodes change

---

## Interview-Ready Explanation

### The Problem I Solved
> "Single-server caches have limits - they can only store so much data, and if the server crashes, all data is lost. I built a system that spreads data across multiple nodes."

### Key Concepts

#### Coordinator-Node Architecture
```
Clients → Coordinator → Nodes
          (Traffic Cop)  (Storage)
```

#### Consistent Hashing (The Buzzword)
> "Instead of random hashing, I used consistent hashing so that when nodes are added/removed, only a small fraction of keys need to move."

**Why this matters:**
- Traditional hashing: 80% of keys move when you add a server
- Consistent hashing: Only ~25% of keys move

**How it works:**
1. Imagine a circle (the "hash ring")
2. Place nodes on the circle
3. Each key falls on a part of the circle → that node owns it
4. Add a new node? It takes over a small slice of the circle

#### Virtual Nodes
> "To distribute data evenly, each physical node has 100 virtual nodes spread around the ring. This prevents one node from getting too much data."

---

## What Was Implemented

### Files Created/Modified

#### 1. CacheConfig.java (NEW)
**Purpose:** Proper bean configuration for cache components

```java
@Configuration
public class CacheConfig {
    @Bean
    @ConditionalOnBean(NodeConfig.class)
    public InMemoryCache inMemoryCache(NodeConfig nodeConfig) { ... }

    @Bean
    @ConditionalOnBean(InMemoryCache.class)
    public CacheLoader cacheLoader(InMemoryCache cache) { ... }

    @Bean
    @ConditionalOnBean(CoordinatorConfig.class)
    public ConsistentHash consistentHash(CoordinatorConfig coordinatorConfig) { ... }
}
```

#### 2. NodeRegistrationService.java (NEW)
**Purpose:** Nodes auto-register with coordinator on startup

```java
@Service
@ConditionalOnProperty(name = "spring.config.activate.on-profile", havingValue = "node")
public class NodeRegistrationService {
    @EventListener(ApplicationReadyEvent.class)
    public void registerWithCoordinator() { ... }
}
```

#### 3. CoordinatorController.java (MODIFIED)
**Added:** Node registration endpoint
```java
@PostMapping("/nodes/register")
public ResponseEntity<String> registerNode(@RequestBody NodeInfo nodeInfo)
```

#### 4. Component Annotations Removed
From these classes (now managed as beans):
- `InMemoryCache.java`
- `CacheLoader.java`
- `ConsistentHash.java`

---

## Testing Guide

### Quick Start - 2 Node Setup

#### Step 1: Start Coordinator
**Terminal 1:**
```bash
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run -Dspring-boot.run.profiles=coordinator
```
Wait for: `Started DistributedCacheApplication`

#### Step 2: Start Node 1
**Terminal 2:**
```bash
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-1 -DNODE_PORT=8081
```
Wait for: `Successfully registered node node-1 with coordinator`

#### Step 3: Run Tests

##### Test 1: List Registered Nodes
```bash
curl http://localhost:8080/coordinator/nodes
```
**Expected Output:**
```json
[{"nodeId":"node-1","host":"localhost","port":8081,"status":"ACTIVE",...}]
```

##### Test 2: Store a Value
```bash
curl -X POST http://localhost:8080/coordinator/cache \
  -H "Content-Type: application/json" \
  -d "{\"key\":\"user:123\",\"value\":\"John Doe\",\"ttlSeconds\":3600}"
```
**Expected Output:**
```json
{"success":true,"key":"user:123","value":"John Doe","message":"Operation successful"}
```

##### Test 3: Retrieve the Value
```bash
curl http://localhost:8080/coordinator/cache/user:123
```
**Expected Output:**
```json
{"success":true,"key":"user:123","value":"John Doe",...}
```

##### Test 4: Check Node Health
```bash
curl http://localhost:8081/node/health
```
**Expected Output:**
```json
{"nodeId":"node-1","healthy":true,"currentSize":1,...}
```

##### Test 5: Delete a Value
```bash
curl -X DELETE http://localhost:8080/coordinator/cache/user:123
```
**Expected Output:**
```json
{"success":true,...}
```

---

### Advanced Testing - Multiple Nodes

#### Start 3 Nodes
**Terminal 2:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-1 -DNODE_PORT=8081
```

**Terminal 3:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-2 -DNODE_PORT=8082
```

**Terminal 4:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-3 -DNODE_PORT=8083
```

#### Test Data Distribution
```bash
# Store multiple keys
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d "{\"key\":\"key1\",\"value\":\"value1\",\"ttlSeconds\":3600}"
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d "{\"key\":\"key2\",\"value\":\"value2\",\"ttlSeconds\":3600}"
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d "{\"key\":\"key3\",\"value\":\"value3\",\"ttlSeconds\":3600}"

# Retrieve them
curl http://localhost:8080/coordinator/cache/key1
curl http://localhost:8080/coordinator/cache/key2
curl http://localhost:8080/coordinator/cache/key3

# See all nodes
curl http://localhost:8080/coordinator/nodes
```

---

### API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/coordinator/cache` | Store a value |
| GET | `/coordinator/cache/{key}` | Retrieve a value |
| DELETE | `/coordinator/cache/{key}` | Delete a value |
| GET | `/coordinator/nodes` | List all nodes |
| POST | `/coordinator/nodes/register` | Register a node |
| GET | `/node/health` | Check node health |

---

## Common Interview Questions

### Q: "Why distributed cache?"
**Answer:**
- **Scalability** - Add more nodes as data grows
- **Reliability** - One node fails, others still work
- **Performance** - Parallel requests, less load per node

### Q: "How do you handle node failures?"
**Answer:**
> "The health monitor periodically checks each node. If a node fails to respond, it's marked as SUSPECTED, then FAILED. New requests avoid that node. When it recovers, it rejoins the ring."

### Q: "How do you prevent duplicate work?"
**Answer:**
> "CacheLoader uses a `ConcurrentHashMap` to track in-flight requests. If multiple threads request the same key, only one actually fetches it; others wait for the result."

### Q: "What happens when you add a new node?"
**Answer:**
> "With consistent hashing, only keys in the new node's range move. Each physical node has 100 virtual nodes for even distribution. The new node auto-registers with the coordinator."

---

## One-Minute Elevator Pitch

> "I built a distributed cache system using consistent hashing. It's like Redis but distributed across multiple nodes. The coordinator routes each key to the appropriate node using a hash ring with virtual nodes for even distribution. It handles node failures gracefully with health monitoring, and uses LRU eviction with TTL for memory management. I used Spring Boot with Java, and nodes auto-register on startup."

---

## Key Interview Buzzwords

✅ **Consistent Hashing** - Even distribution, minimal remapping
✅ **Virtual Nodes** - Better load balancing
✅ **Coordinator-Node Architecture** - Separation of concerns
✅ **Health Monitoring** - Fault tolerance
✅ **LRU Cache with TTL** - Memory management
✅ **Auto-registration** - Dynamic scaling
✅ **Thread-safe operations** - `ConcurrentSkipListSet`, `ConcurrentHashMap`

---

## What This Shows Interviewers

This project demonstrates you understand:
- **Distributed systems** concepts
- **Scalability** strategies
- **Fault tolerance** patterns
- **Spring Boot** + **Java** expertise
- **Concurrent programming** (thread-safe collections)
- **REST API design**

It's a solid conversation starter for systems design discussions!

---

## Quick Command Reference

| Action | Command |
|--------|---------|
| Start Coordinator | `mvn spring-boot:run -Dspring-boot.run.profiles=coordinator` |
| Start Node | `mvn spring-boot:run -Dspring-boot.run.profiles=node -DNODE_ID=node-1 -DNODE_PORT=8081` |
| Store Value | `curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d "{\"key\":\"k\",\"value\":\"v\",\"ttlSeconds\":3600}"` |
| Get Value | `curl http://localhost:8080/coordinator/cache/{key}` |
| Delete Value | `curl -X DELETE http://localhost:8080/coordinator/cache/{key}` |
| List Nodes | `curl http://localhost:8080/coordinator/nodes` |
| Check Health | `curl http://localhost:8081/node/health` |
| Run Tests | `mvn test` |
| Run Integration Test | `mvn test -Dtest=DistributedCacheIntegrationTest` |
