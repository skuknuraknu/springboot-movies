package com.gugugaga.gateway.dto;

public class UserSubscriptionResponse {
    private Long maxRequest;
    private String name;
    private String status; // ACTIVE, EXPIRED, etc.
    public Long getMaxRequest() {
        return maxRequest;
    }
    public void setMaxRequest(Long maxRequest) {
        this.maxRequest = maxRequest;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
