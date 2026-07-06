package com.cache.distributed.hashring;

import com.cache.distributed.model.NodeInfo;

public class VirtualNode implements Comparable<VirtualNode> {
    private final long hash;
    private final NodeInfo physicalNode;
    private final int replicaIndex;

    public VirtualNode(long hash, NodeInfo physicalNode, int replicaIndex) {
        this.hash = hash;
        this.physicalNode = physicalNode;
        this.replicaIndex = replicaIndex;
    }

    public long getHash() {
        return hash;
    }

    public NodeInfo getPhysicalNode() {
        return physicalNode;
    }

    public int getReplicaIndex() {
        return replicaIndex;
    }

    @Override
    public int compareTo(VirtualNode other) {
        return Long.compareUnsigned(this.hash, other.hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualNode that = (VirtualNode) o;
        return hash == that.hash && replicaIndex == that.replicaIndex &&
                (physicalNode != null ? physicalNode.equals(that.physicalNode) : that.physicalNode == null);
    }

    @Override
    public int hashCode() {
        int result = (int) (hash ^ (hash >>> 32));
        result = 31 * result + (physicalNode != null ? physicalNode.hashCode() : 0);
        result = 31 * result + replicaIndex;
        return result;
    }
}
