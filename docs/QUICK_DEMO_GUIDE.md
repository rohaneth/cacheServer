# Quick Demo Guide - 3 Key Features

## Setup First

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

## Part 1: Show Consistent Hashing

### Step 1: Store 5 Keys (All go to node-1)
```powershell
Write-Host "=== Storing 5 keys (all should route to node-1) ==="
for ($i=1; $i -le 5; $i++) {
  $result = Invoke-RestMethod -Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"key$i\",\"value\":\"value$i\",\"ttlSeconds":3600}"
  Write-Host "key$i → node: $($result.nodeId)"
}
```
**Expected Output:**
```
key1 → node: node-1
key2 → node: node-1
key3 → node: node-1
key4 → node: node-1
key5 → node: node-1
```

---

### Step 2: Start Node-2 (Terminal 3)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-2" "-DNODE_PORT=8082"
```
**Wait for:** `Successfully registered node node-2 with coordinator`

---

### Step 3: Verify Both Nodes
```powershell
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, port, status
```
**Expected:** Shows both node-1 and node-2

---

### Step 4: Store 5 More Keys (Distribute across both)
```powershell
Write-Host "=== Storing 5 more keys (should distribute across both nodes) ==="
for ($i=6; $i -le 10; $i++) {
  $result = Invoke-RestMethod -Post -Uri "http://localhost:8080/coordinator/cache" `
    -ContentType "application/json" `
    -Body "{\"key\":\"key$i\",\"value\":\"value$i\",\"ttlSeconds\":3600}"
  Write-Host "key$i → node: $($result.nodeId)"
}
```
**Expected Output:**
```
key6 → node: node-2
key7 → node: node-1
key8 → node: node-2
key9 → node: node-1
key10 → node: node-2
```
(Or similar distribution - keys now spread across both nodes!)

---

### Step 5: Old Keys Still Work (Proves Consistent Hashing)
```powershell
Write-Host "=== Retrieving old keys (stored before node-2 existed) ==="
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key3" | Select-Object key, value
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key5" | Select-Object key, value
```
**Expected:** Both return values correctly (even though node-2 was added later)

**✅ Consistent Hashing Proven:**
- Adding a node doesn't break existing data
- New keys distribute across all nodes
- Old keys still route correctly

---

## Part 2: Show Fault Tolerance

### Step 1: Check Current Node Status
```powershell
Write-Host "=== Current node status ==="
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
```
**Expected:** Both nodes show as ACTIVE

---

### Step 2: Stop Node-1
Go to **Terminal 2** and press `Ctrl+C`

---

### Step 3: Wait for Failure Detection
```powershell
Write-Host "Waiting 15 seconds for health monitor to detect failure..."
Start-Sleep -Seconds 15
```

---

### Step 4: Show Coordinator Knows Node-1 is Down
```powershell
Write-Host "=== Node status after failure ==="
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
```
**Expected:** node-1 shows as SUSPECTED or FAILED

---

### Step 5: Other Node Still Works
```powershell
Write-Host "=== Testing if node-2 still works ==="
Invoke-RestMethod -Get "http://localhost:8082/node/health" | Select-Object nodeId, healthy
```
**Expected:** node-2 shows healthy=true

---

### Step 6: Restart Node-1 (Terminal 2)
```powershell
cd "E:\GOPROGRAM\Spring boot projects\cacheServer"
mvn spring-boot:run "-Dspring-boot.run.profiles=node" "-DNODE_ID=node-1" "-DNODE_PORT=8081"
```
**Wait for:** `Successfully registered node node-1 with coordinator`

---

### Step 7: Show Node-1 Auto-Rejoined
```powershell
Write-Host "=== Node status after recovery ==="
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
```
**Expected:** node-1 shows as ACTIVE again

---

### Step 8: Node-1 Works Again
```powershell
Write-Host "=== Testing node-1 recovery ==="
Invoke-RestMethod -Get "http://localhost:8081/node/health" | Select-Object nodeId, healthy
```
**Expected:** node-1 shows healthy=true

**✅ Fault Tolerance Proven:**
- Coordinator detects node failures
- Other nodes continue working
- Failed nodes auto-recover on restart

---

## Part 3: Show TTL (Time-To-Live)

### Step 1: Store Key with 5 Second TTL
```powershell
Write-Host "=== Storing key with 5 second TTL ==="
Invoke-RestMethod -Post -Uri "http://localhost:8080/coordinator/cache" `
  -ContentType "application/json" `
  -Body '{"key":"temp","value":"I will expire","ttlSeconds":5}' | Select-Object success, key
```
**Expected:** success=true

---

### Step 2: Retrieve Before Expiration
```powershell
Write-Host "=== Retrieving immediately (should work) ==="
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp" | Select-Object key, value
```
**Expected:** Returns the value "I will expire"

---

### Step 3: Wait for Expiration
```powershell
Write-Host "Waiting 6 seconds for key to expire..."
Start-Sleep -Seconds 6
```

---

### Step 4: Try to Retrieve Expired Key
```powershell
Write-Host "=== Retrieving after expiration (should be gone) ==="
try {
  Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp"
} catch {
  Write-Host "Key not found or expired (expected behavior)"
}
```
**Expected:** Error or "not found" (key has expired)

**✅ TTL Proven:**
- Keys expire after specified time
- Expired keys are automatically removed

---

## Summary - What You Proved

| Feature | How You Proved It |
|---------|------------------|
| **Consistent Hashing** | Keys distributed evenly, old keys still work after adding nodes |
| **Fault Tolerance** | Node failures detected, other nodes unaffected, auto-recovery works |
| **TTL** | Keys expire after time limit, automatically removed |

---

## Cleanup (When Done)

```powershell
# Stop all processes
$ports = @(8080, 8081, 8082)
foreach ($port in $ports) {
  $proc = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
          Select-Object -ExpandProperty OwningProcess
  if ($proc) {
    Stop-Process -Id $proc -Force
    Write-Host "Stopped process on port $port"
  }
}
```

---

## Quick Copy-Paste Commands

### Consistent Hashing (Full)
```powershell
# Store 5 keys
for ($i=1; $i -le 5; $i++) {
  $r = Invoke-RestMethod -Post "http://localhost:8080/coordinator/cache" -ContentType "application/json" -Body "{\"key\":\"key$i\",\"value\":\"v\",\"ttlSeconds\":3600}"
  Write-Host "key$i → $($r.nodeId)"
}
# (Start node-2 in new terminal)
# Store 5 more
for ($i=6; $i -le 10; $i++) {
  $r = Invoke-RestMethod -Post "http://localhost:8080/coordinator/cache" -ContentType "application/json" -Body "{\"key\":\"key$i\",\"value\":\"v\",\"ttlSeconds\":3600}"
  Write-Host "key$i → $($r.nodeId)"
}
# Verify old keys still work
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/key3"
```

### Fault Tolerance (Full)
```powershell
# Check status
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
# Stop node-1 (Ctrl+C in terminal)
Start-Sleep -Seconds 15
# Check status again
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
# Restart node-1 (in terminal)
Invoke-RestMethod -Get "http://localhost:8080/coordinator/nodes" | Select-Object nodeId, status
```

### TTL (Full)
```powershell
# Store with 5s TTL
Invoke-RestMethod -Post "http://localhost:8080/coordinator/cache" -ContentType "application/json" -Body '{"key":"temp","value":"v","ttlSeconds":5}'
# Get immediately
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp"
# Wait and try again
Start-Sleep -Seconds 6
Invoke-RestMethod -Get "http://localhost:8080/coordinator/cache/temp"
```

---

**Ready to demo! 🚀**
