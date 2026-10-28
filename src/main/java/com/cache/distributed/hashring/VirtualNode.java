package com.cache.distributed.hashring;

import com.cache.distributed.model.NodeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualNode implements Comparable<VirtualNode> {
    private long hash;
    private NodeInfo physicalNode;
    private int replicaIndex;

    @Override
    public int compareTo(VirtualNode other) {
        return Long.compareUnsigned(this.hash, other.hash);
    }
}
