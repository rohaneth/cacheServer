# Distributed Cache - Complete Testing Guide (From Scratch)

## Prerequisites
- Java 17+ installed
- Maven installed
- PowerShell or terminal

---

## Phase 1: Start the Applications

### Step 1: Open Terminal 1 - Start Coordinator
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=coordinator"
```

**Wait for:** `Started DistributedCacheApplication in X seconds`

**Expected Output:**
```
Tomcat started on port 8080 (http) with context path ''
Started DistributedCacheApplication in 4.731 seconds
```

---

### Step 2: Open Terminal 2 - Start Node-1
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"
```

**Wait for:** `Successfully registered node node-1 with coordinator`

**Expected Output:**
```
Tomcat started on port 8081 (http) with context path ''
Successfully registered node node-1 with coordinator
```

---

### Step 3: (Optional) Open Terminal 3 - Start Node-2
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-2" "-DNODE_PORT=8082"
```

---

### Step 4: (Optional) Open Terminal 4 - Start Node-3
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-3" "-DNODE_PORT=8083"
```

---

## Phase 2: Verify Applications Started

### Test 1: Check Coordinator Health
```powershell
curl http://localhost:8080/actuator/health
```

**Expected Output:**
```json
{"status":"UP"}
```

---

### Test 2: Check Node-1 Health
```powershell
curl http://localhost:8081/node/health
```

**Expected Output:**
```json
{
  "nodeId": "node-1",
  "healthy": true,
  "currentSize": 0,
  "timestamp": "2026-07-08T..."
}
```

---

### Test 3: List All Registered Nodes
```powershell
curl http://localhost:8080/coordinator/nodes
```

**Expected Output:**
```json
[
  {
    "nodeId": "node-1",
    "host": "localhost",
    "port": 8081,
    "status": "ACTIVE",
    "lastHeartbeat": "2026-07-08T...",
    "virtualNodeCount": 100
  }
]
```

---

## Phase 3: Basic CRUD Operations

### Test 4: Store a Value
```powershell
curl -X POST http://localhost:8080/coordinator/cache `
  -H "Content-Type: application/json" `
  -d '{"key":"user:123","value":"John Doe","ttlSeconds":3600}'
```

**Expected Output:**
```json
{
  "success": true,
  "key": "user:123",
  "value": "John Doe",
  "message": "Operation successful",
  "nodeId": "node-1"
}
```

---

### Test 5: Retrieve the Value
```powershell
curl http://localhost:8080/coordinator/cache/user:123
```

**Expected Output:**
```json
{
  "success": true,
  "key": "user:123",
  "value": "John Doe",
  "message": "Operation successful"
}
```

---

### Test 6: Update a Value
```powershell
curl -X POST http://localhost:8080/coordinator/cache `
  -H "Content-Type: application/json" `
  -d '{"key":"user:123","value":"Jane Smith","ttlSeconds":3600}'
```

**Expected Output:**
```json
{
  "success": true,
  "key": "user:123",
  "value": "Jane Smith",
  "message": "Operation successful"
}
```

---

### Test 7: Delete a Value
```powershell
curl -X DELETE http://localhost:8080/coordinator/cache/user:123
```

**Expected Output:**
```json
{
  "success": true,
  "key": "user:123",
  "value": null,
  "message": "Operation successful"
}
```

---

### Test 8: Try to Retrieve Deleted Value
```powershell
curl http://localhost:8080/coordinator/cache/user:123
```

**Expected Output:** `404 Not Found` or error message

---

## Phase 4: Advanced Tests

### Test 9: Store Multiple Keys (Test Distribution)
```powershell
# Store 10 keys
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"key1","value":"value1","ttlSeconds":3600}'
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"key2","value":"value2","ttlSeconds":3600}'
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"key3","value":"value3","ttlSeconds":3600}'
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"key4","value":"value4","ttlSeconds":3600}'
curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"key5","value":"value5","ttlSeconds":3600}'
```

---

### Test 10: Verify All Keys
```powershell
curl http://localhost:8080/coordinator/cache/key1
curl http://localhost:8080/coordinator/cache/key2
curl http://localhost:8080/coordinator/cache/key3
curl http://localhost:8080/coordinator/cache/key4
curl http://localhost:8080/coordinator/cache/key5
```

---

### Test 11: Test TTL Expiration
```powershell
# Store value with 5 second TTL
curl -X POST http://localhost:8080/coordinator/cache `
  -H "Content-Type: application/json" `
  -d '{"key":"temp","value":"expires soon","ttlSeconds":5}'

# Wait 6 seconds
Start-Sleep -Seconds 6

# Try to retrieve (should be expired)
curl http://localhost:8080/coordinator/cache/temp
```

**Expected:** Key not found or expired

---

### Test 12: Test Node Health Under Load
```powershell
# Store 100 values quickly
for ($i=1; $i -le 100; $i++) {
  curl -X POST http://localhost:8080/coordinator/cache `
    -H "Content-Type: application/json" `
    -d "{\"key\":\"loadtest$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
}

# Check node health
curl http://localhost:8081/node/health
```

**Expected:** `currentSize` should show ~100 entries

---

## Phase 5: Multi-Node Tests (Requires 3+ Nodes)

### Test 13: Start Multiple Nodes
If you haven't already, start Node-2 and Node-3:
```powershell
# Terminal 3
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-2" "-DNODE_PORT=8082"

# Terminal 4
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-3" "-DNODE_PORT=8083"
```

---

### Test 14: Verify All Nodes Registered
```powershell
curl http://localhost:8080/coordinator/nodes
```

**Expected Output:** Should show 3 nodes

---

### Test 15: Test Data Distribution
```powershell
# Store many keys and see which node handles each
for ($i=1; $i -le 20; $i++) {
  $response = curl -X POST http://localhost:8080/coordinator/cache `
    -H "Content-Type: application/json" `
    -d "{\"key\":\"dist$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
  Write-Host "Key dist$i -> $response"
}
```

**Observe:** Keys should be distributed across all 3 nodes

---

## Phase 6: Fault Tolerance Tests

### Test 16: Simulate Node Failure
```powershell
# Stop Node-1 (Ctrl+C in Terminal 2)

# Wait a few seconds
Start-Sleep -Seconds 5

# Try to store/retrieve (should fail or reroute)
curl -X POST http://localhost:8080/coordinator/cache `
  -H "Content-Type: application/json" `
  -d '{"key":"test","value":"should fail","ttlSeconds":3600}'
```

---

### Test 17: Node Recovery
```powershell
# Restart Node-1
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"

# Verify it re-registered
curl http://localhost:8080/coordinator/nodes

# Test operations work again
curl -X POST http://localhost:8080/coordinator/cache `
  -H "Content-Type: application/json" `
  -d '{"key":"recovery","value":"back online","ttlSeconds":3600}'
```

---

## Phase 7: Unit & Integration Tests

### Test 18: Run All Unit Tests
```powershell
mvn test
```

**Expected:** All tests pass ✅

---

### Test 19: Run Specific Tests
```powershell
# Test consistent hashing
mvn test -Dtest=ConsistentHashTest

# Test in-memory cache
mvn test -Dtest=InMemoryCacheTest

# Test cache loader
mvn test -Dtest=CacheLoaderTest

# Test coordinator service
mvn test -Dtest=CoordinatorServiceTest

# Test node registry
mvn test -Dtest=NodeRegistryTest
```

---

### Test 20: Run Integration Tests
```powershell
mvn test -Dtest=DistributedCacheIntegrationTest
```

---

## Phase 8: Performance Tests

### Test 21: Measure Latency
```powershell
# Measure time for 100 operations
$startTime = Get-Date
for ($i=1; $i -le 100; $i++) {
  curl http://localhost:8080/coordinator/cache/key$i > $null
}
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalMilliseconds
Write-Host "100 GETs took $($duration)ms"
Write-Host "Average: $($duration/100)ms per operation"
```

---

### Test 22: Concurrent Operations
```powershell
# Start 10 parallel curl jobs
for ($i=1; $i -le 10; $i++) {
  Start-Job -ScriptBlock {
    param($id)
    for ($j=1; $j -le 10; $j++) {
      curl -X POST http://localhost:8080/coordinator/cache `
        -H "Content-Type: application/json" `
        -d "{\"key\":\"concurrent${id}_${j}\",\"value\":\"value\",\"ttlSeconds\":3600}"
    }
  } -ArgumentList $i
}

# Wait for all to complete
Get-Job | Wait-Job | Receive-Job
```

---

## Phase 9: Cleanup

### Stop All Applications
```powershell
# Press Ctrl+C in each terminal, OR use this command:
$ports = @(8080, 8081, 8082, 8083)
foreach ($port in $ports) {
  $proc = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | 
          Select-Object -ExpandProperty OwningProcess
  if ($proc) { Stop-Process -Id $proc -Force }
}
```

---

## Quick Test Command Reference

| Test | Command |
|------|---------|
| **Start Coordinator** | `mvn spring-boot:run "-Dspring-boot.run.profiles=coordinator"` |
| **Start Node** | `mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"` |
| **Health Check** | `curl http://localhost:8080/actuator/health` |
| **List Nodes** | `curl http://localhost:8080/coordinator/nodes` |
| **Store Value** | `curl -X POST http://localhost:8080/coordinator/cache -H "Content-Type: application/json" -d '{"key":"k","value":"v","ttlSeconds":3600}'` |
| **Get Value** | `curl http://localhost:8080/coordinator/cache/{key}` |
| **Delete Value** | `curl -X DELETE http://localhost:8080/coordinator/cache/{key}` |
| **Node Health** | `curl http://localhost:8081/node/health` |
| **Run Tests** | `mvn test` |

---

## Expected Results Summary

✅ Coordinator starts on port 8080
✅ Nodes start and auto-register
✅ Can store, retrieve, and delete values
✅ Values expire after TTL
✅ Multiple nodes distribute data evenly
✅ Node failures are detected
✅ Nodes can rejoin after recovery
✅ All unit tests pass
✅ Integration tests pass

---

## Troubleshooting

### Issue: "Connection refused"
**Solution:** Make sure the application is running. Check with `netstat -ano | findstr :8080`

### Issue: "No nodes available"
**Solution:** Make sure at least one node is running and registered

### Issue: Port already in use
**Solution:** Stop the process using that port or use a different port

### Issue: Build failure
**Solution:** Run `mvn clean install` first

---

## Test Checklist

Use this checklist to ensure comprehensive testing:

- [ ] Coordinator starts successfully
- [ ] Node-1 starts successfully
- [ ] Node auto-registers with coordinator
- [ ] Can store a value
- [ ] Can retrieve a value
- [ ] Can update a value
- [ ] Can delete a value
- [ ] TTL expiration works
- [ ] Multiple nodes register
- [ ] Data distributes across nodes
- [ ] Node health monitoring works
- [ ] Failed nodes are detected
- [ ] Nodes can recover and rejoin
- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Concurrent operations work

---

**Happy Testing! 🧪**
