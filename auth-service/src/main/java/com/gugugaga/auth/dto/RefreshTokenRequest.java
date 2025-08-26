package com.gugugaga.auth.dto;

// 6. DTOs for request/response
public class RefreshTokenRequest {
    private String refreshToken;
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { 
        this.refreshToken = refreshToken; 
    }
}
