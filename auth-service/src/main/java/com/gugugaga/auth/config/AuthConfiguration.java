package com.gugugaga.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth")
@EnableConfigurationProperties
public class AuthConfiguration {
    
    private Jwt jwt = new Jwt();
    private Messages messages = new Messages();
    
    public Jwt getJwt() {
        return jwt;
    }
    
    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }
    
    public Messages getMessages() {
        return messages;
    }
    
    public void setMessages(Messages messages) {
        this.messages = messages;
    }
    
    public static class Jwt {
        private String secretKey = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkw";
        private long accessTokenExpirationMs = 1000 * 60 * 60; // 1 hour
        private long refreshTokenExpirationMs = 1000 * 60 * 60 * 24 * 7; // 7 days
        
        public String getSecretKey() {
            return secretKey;
        }
        
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
        
        public long getAccessTokenExpirationMs() {
            return accessTokenExpirationMs;
        }
        
        public void setAccessTokenExpirationMs(long accessTokenExpirationMs) {
            this.accessTokenExpirationMs = accessTokenExpirationMs;
        }
        
        public long getRefreshTokenExpirationMs() {
            return refreshTokenExpirationMs;
        }
        
        public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) {
            this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        }
    }
    
    public static class Messages {
        private String tokenGenerationPrefix = "Generating token for user: ";
        private String tokenValidationSuccess = "Token validation result: ";
        private String tokenValidationError = "Error validating token: ";
        private String tokenParsingError = "Error parsing token: ";
        private String secretKeyDebugPrefix = "Secret key being used: ";
        
        public String getTokenGenerationPrefix() {
            return tokenGenerationPrefix;
        }
        
        public void setTokenGenerationPrefix(String tokenGenerationPrefix) {
            this.tokenGenerationPrefix = tokenGenerationPrefix;
        }
        
        public String getTokenValidationSuccess() {
            return tokenValidationSuccess;
        }
        
        public void setTokenValidationSuccess(String tokenValidationSuccess) {
            this.tokenValidationSuccess = tokenValidationSuccess;
        }
        
        public String getTokenValidationError() {
            return tokenValidationError;
        }
        
        public void setTokenValidationError(String tokenValidationError) {
            this.tokenValidationError = tokenValidationError;
        }
        
        public String getTokenParsingError() {
            return tokenParsingError;
        }
        
        public void setTokenParsingError(String tokenParsingError) {
            this.tokenParsingError = tokenParsingError;
        }
        
        public String getSecretKeyDebugPrefix() {
            return secretKeyDebugPrefix;
        }
        
        public void setSecretKeyDebugPrefix(String secretKeyDebugPrefix) {
            this.secretKeyDebugPrefix = secretKeyDebugPrefix;
        }
    }
}