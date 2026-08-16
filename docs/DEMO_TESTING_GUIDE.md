# Distributed Cache - Complete Demo Testing Guide

This guide shows what makes this distributed cache special through practical tests.

---

## Prerequisites
- Java 17+ installed
- Maven installed
- PowerShell terminal

---

## Part 1: Setup - Start the Applications

### Step 1: Start Coordinator (Terminal 1)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=coordinator"
```
**Wait for:** `Started DistributedCacheApplication`

---

### Step 2: Start Node-1 (Terminal 2)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"
```
**Wait for:** `Successfully registered node node-1 with coordinator`

---

## Part 2: Verify Basic Setup

### Test 1: Check Registered Nodes
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes"
```
**Expected:** Shows node-1

---

### Test 2: Check Node Health
```powershell
Invoke-RestMethod -Get "http://localhost:8081/node/health"
```
**Expected:** Shows healthy=true, currentSize=0

---

## Part 3: Basic CRUD Operations

### Test 3: Store a Value
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
  -ContentType "application/json" `
  -Body '{"key":"user:123","value":"John Doe","ttlSeconds":3600}'
```
**Expected:** success=true

---

### Test 4: Retrieve the Value
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/user:123"
```
**Expected:** Returns "John Doe"

---

### Test 5: Delete the Value
```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/coordinator/cache/user:123"
```
**Expected:** success=true

---

## Part 4: Show Consistent Hashing (The Magic!)

This demonstrates that the same key always routes to the same node.

### Test 6: Store Multiple Keys
```powershell
for ($i=1; $i -le 10; $i++) {
  $result = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"key$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
  Write-Host "Stored key$i on node: $($result.nodeId)"
}
```
**Observe:** All keys route to node-1 (only node available)

---

### Test 7: Add a Second Node (Terminal 3)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-2" "-DNODE_PORT=8082"
```
**Wait for:** `Successfully registered node node-2 with coordinator`

---

### Test 8: Verify Both Nodes Registered
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | ConvertTo-Json
```
**Expected:** Shows both node-1 and node-2

---

### Test 9: Store More Keys (Shows Distribution)
```powershell
for ($i=11; $i -le 20; $i++) {
  $result = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"key$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
  Write-Host "Stored key$i on node: $($result.nodeId)"
}
```
**Observe:** Keys distribute across BOTH nodes!

---

### Test 10: Old Keys Still Work (Minimal Disruption)
```powershell
# Try to retrieve keys stored before node-2 existed
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key5"
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key3"
```
**Expected:** Still works! This is consistent hashing in action.

---

## Part 5: Show Dynamic Scaling

### Test 11: Add Third Node (Terminal 4)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-3" "-DNODE_PORT=8083"
```
**Wait for:** `Successfully registered node node-3 with coordinator`

---

### Test 12: Verify Three Nodes
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | ConvertTo-Json
```
**Expected:** Shows node-1, node-2, node-3

---

### Test 13: New Keys Distribute Across Three Nodes
```powershell
for ($i=21; $i -le 30; $i++) {
  $result = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"key$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
  Write-Host "Stored key$i on node: $($result.nodeId)"
}
```
**Observe:** Keys now spread across 3 nodes!

---

### Test 14: Old Keys Still Work (Again!)
```powershell
# Keys from Part 4 still work even with 3 nodes
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key5"
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key15"
```
**Expected:** All keys still accessible - minimal disruption from adding nodes.

---

## Part 6: Show TTL (Time-To-Live)

### Test 15: Store Key with Short TTL
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
  -ContentType "application/json" `
  -Body '{"key":"temp","value":"I will expire","ttlSeconds":5}'
```
**Expected:** success=true

---

### Test 16: Retrieve Before Expiration
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp"
```
**Expected:** Returns the value

---

### Test 17: Wait for Expiration
```powershell
Write-Host "Waiting 6 seconds for key to expire..."
Start-Sleep -Seconds 6
```

---

### Test 18: Try to Retrieve Expired Key
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp"
```
**Expected:** Key not found or error (expired and evicted)

---

## Part 7: Show Node Health Monitoring

### Test 19: Check All Node Health
```powershell
Invoke-RestMethod -Get "http://localhost:8081/node/health"
Invoke-RestMethod -Get "http://localhost:8082/node/health"
Invoke-RestMethod -Get "http://localhost:8083/node/health"
```
**Expected:** All show healthy=true with currentSize > 0

---

### Test 20: See Current Cache Sizes
```powershell
Invoke-RestMethod -Get "http://localhost:8081/node/health" | Select-Object nodeId, currentSize
Invoke-RestMethod -Get "http://localhost:8082/node/health" | Select-Object nodeId, currentSize
Invoke-RestMethod -Get "http://localhost:8083/node/health" | Select-Object nodeId, currentSize
```
**Observe:** Each node has different number of entries (shows distribution!)

---

## Part 8: Show Fault Tolerance

### Test 21: Stop Node-1 (Ctrl+C in Terminal 2)

---

### Test 22: Wait for Failure Detection
```powershell
Write-Host "Waiting for health monitor to detect failure..."
Start-Sleep -Seconds 15
```

---

### Test 23: Check Node Status (Shows Failed Node)
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | ConvertTo-Json
```
**Expected:** node-1 shows as SUSPECTED or FAILED

---

### Test 24: Try to Access Data (Graceful Failure)
```powershell
# Try to get data that was on node-1
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key5"
```
**Expected:** Error message (node unavailable)

---

### Test 25: But Other Nodes Still Work!
```powershell
# Keys on other nodes still work
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key15"
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key25"
```
**Expected:** These still work! (They're on healthy nodes)

---

### Test 26: Restart Node-1 (Terminal 2 Again)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"
```
**Wait for:** `Successfully registered node node-1 with coordinator`

---

### Test 27: Verify Recovery
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | ConvertTo-Json
```
**Expected:** node-1 back as ACTIVE

---

### Test 28: Old Keys Work Again
```powershell
# Now key5 works again!
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key5"
```
**Expected:** Returns the value (node recovered!)

---

## Part 9: Show Load Distribution

### Test 29: Store 100 Keys (Shows Even Distribution)
```powershell
$node1Count = 0
$node2Count = 0
$node3Count = 0

for ($i=100; $i -le 200; $i++) {
  $result = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"loadtest$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"

  if ($result.nodeId -eq "node-1") { $node1Count++ }
  elseif ($result.nodeId -eq "node-2") { $node2Count++ }
  elseif ($result.nodeId -eq "node-3") { $node3Count++ }
}

Write-Host "Distribution:"
Write-Host "Node-1: $node1Count keys"
Write-Host "Node-2: $node2Count keys"
Write-Host "Node-3: $node3Count keys"
```
**Observe:** Roughly equal distribution (thanks to virtual nodes!)

---

### Test 30: Verify via Node Health
```powershell
Invoke-RestMethod -Get "http://localhost:8081/node/health" | Select-Object nodeId, currentSize
Invoke-RestMethod -Get "http://localhost:8082/node/health" | Select-Object nodeId, currentSize
Invoke-RestMethod -Get "http://localhost:8083/node/health" | Select-Object nodeId, currentSize
```
**Expected:** Each has ~30-40 entries

---

## Part 10: Cleanup

### Stop All Applications
```powershell
# Press Ctrl+C in each terminal, OR:
$ports = @(8080, 8081, 8082, 8083)
foreach ($port in $ports) {
  $proc = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | 
          Select-Object -ExpandProperty OwningProcess
  if ($proc) { Stop-Process -Id $proc -Force; Write-Host "Stopped process on port $port" }
}
```

---

## Test Summary Checklist

Use this to track your progress:

### Part 2: Basic Setup ✅
- [ ] Coordinator running on port 8080
- [ ] Node-1 running on port 8081
- [ ] Node registered with coordinator

### Part 3: CRUD ✅
- [ ] Store value works
- [ ] Retrieve value works
- [ ] Delete value works

### Part 4: Consistent Hashing ✅
- [ ] Keys route to correct node
- [ ] Adding node-2 distributes new keys
- [ ] Old keys still accessible after adding node

### Part 5: Dynamic Scaling ✅
- [ ] Adding node-3 works automatically
- [ ] Keys distribute across 3 nodes
- [ ] No disruption to existing data

### Part 6: TTL ✅
- [ ] Keys expire after TTL
- [ ] Expired keys return error

### Part 7: Health Monitoring ✅
- [ ] Can check individual node health
- [ ] Each node shows different cache size
- [ ] Distribution is roughly even

### Part 8: Fault Tolerance ✅
- [ ] Stopping node-1 is detected
- [ ] Other nodes continue working
- [ ] Restarting node-1 recovers data
- [ ] Failed keys work after recovery

### Part 9: Load Distribution ✅
- [ ] 100 keys distribute evenly
- [ ] Each node gets ~1/3 of data

---

## Interview Demo Script

Use this 5-minute demo in interviews:

**Minute 1: Setup**
> "Let me start the coordinator and a node..."

**Minute 2: Basic Operations**
> "I can store and retrieve values. Now let me add a second node..."

**Minute 3: Consistent Hashing**
> "Watch - these new keys distribute across both nodes automatically. And the old keys still work! That's consistent hashing."

**Minute 4: Fault Tolerance**
> "If I stop a node, the system detects it. The other nodes keep working. When it comes back, it auto-recovers."

**Minute 5: Summary**
> "So we have: consistent hashing, fault tolerance, dynamic scaling, and health monitoring. Not just CRUD - a distributed system."

---

## Quick Command Reference (PowerShell)

| Action | Command |
|--------|---------|
| **Start Coordinator** | `mvn spring-boot:run "-Dspring-boot.run.profiles=coordinator"` |
| **Start Node** | `mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-X" "-DNODE_PORT=808X"` |
| **List Nodes** | `Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes"` |
| **Store** | `Invoke-RestMethod -Post "http://localhost:8080/coordinator/cache" -ContentType "application/json" -Body '{"key":"k","value":"v","ttlSeconds":3600}'` |
| **Get** | `Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key"` |
| **Delete** | `Invoke-RestMethod -Delete "http://localhost:8080/coordinator/cache/key"` |
| **Health** | `Invoke-RestMethod -Get "http://localhost:808X/node/health"` |
| **Stop All** | `$ports = @(8080,8081,8082,8083); foreach ($p in $ports) { Stop-Process -Id (Get-NetTCPConnection -LocalPort $p -State Listen).OwningProcess -Force }` |

---

**Happy Testing! 🧪**
