package com.cache.distributed.coordinator;

import com.cache.distributed.model.NodeInfo;

public class RoutingDecision {
    private String key;
    private NodeInfo targetNode;
    private String targetUrl;

    public RoutingDecision() {
    }

    public RoutingDecision(String key, NodeInfo targetNode, String targetUrl) {
        this.key = key;
        this.targetNode = targetNode;
        this.targetUrl = targetUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public NodeInfo getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(NodeInfo targetNode) {
        this.targetNode = targetNode;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
