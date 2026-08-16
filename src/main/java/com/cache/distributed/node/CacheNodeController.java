package com.cache.distributed.node;

import com.cache.distributed.model.CacheRequest;
import com.cache.distributed.model.CacheResponse;
import com.cache.distributed.model.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node")
public class CacheNodeController {

    private final CacheNodeService nodeService;

    @Autowired
    public CacheNodeController(CacheNodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping("/cache")
    public ResponseEntity<CacheResponse> put(@RequestBody CacheRequest request) {
        CacheResponse response = nodeService.put(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> get(@PathVariable String key) {
        CacheResponse response = nodeService.get(key);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/cache/{key}")
    public ResponseEntity<CacheResponse> delete(@PathVariable String key) {
        CacheResponse response = nodeService.delete(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        return ResponseEntity.ok(nodeService.getHealthStatus());
    }
}
