package com.cache.distributed.coordinator;

import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/coordinator")
public class CoordinatorController {

    private final CoordinatorService coordinatorService;

    @Autowired
    public CoordinatorController(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @PostMapping("/cache")
    public ResponseEntity<CacheResponse> put(@RequestBody CacheRequest request) {
        CacheResponse response = coordinatorService.put(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> get(@PathVariable String key) {
        CacheResponse response = coordinatorService.get(key);

        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> delete(@PathVariable String key) {
        CacheResponse response = coordinatorService.delete(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<Set<NodeInfo>> getNodes() {
        return ResponseEntity.ok(coordinatorService.getAllNodes());
    }

    @PostMapping("/nodes/register")
    public ResponseEntity<String> registerNode(@RequestBody NodeInfo nodeInfo) {
        coordinatorService.addNode(nodeInfo);
        return ResponseEntity.ok("Node registered: " + nodeInfo.getNodeId());
    }
}
