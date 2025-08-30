package com.gugugaga.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    
    public JwtFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            System.out.println("🔍 JWT Filter called for path: " + path); // Add this log
            // Skip JWT validation for public endpoints
            if (isPublicEndpoint(path, config.getPublicEndpoints())) {
                return chain.filter(exchange);
            }
            
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return sendErrorResponse(exchange, "Missing or invalid Authorization header", "MISSING_TOKEN");
            }
            
            try {
                String token = authHeader.substring(7);
                
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String userId = jwtUtil.extractUserId(token);
                    
                    // Add user info to headers for downstream services
                    ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r
                            .header("X-Username", username)
                            .header("X-User-Id", userId != null ? userId : "")
                        )
                        .build();
                    
                    return chain.filter(modifiedExchange);
                } else {
                    return sendErrorResponse(exchange, "Invalid or expired token", "INVALID_TOKEN");
                }
                
            } catch (Exception e) {
                return sendErrorResponse(exchange, "Token validation failed", "TOKEN_ERROR");
            }
        };
    }
    
    private boolean isPublicEndpoint(String path, List<String> publicEndpoints) {
        if (publicEndpoints == null) {
            return false;
        }
          return publicEndpoints.stream().anyMatch(publicPath -> {
            // Handle wildcard patterns like /api/auth/subscription/**
            if (publicPath.endsWith("/**")) {
                String basePattern = publicPath.substring(0, publicPath.length() - 3);
                return path.startsWith(basePattern);
            }
            // Handle exact matches
            return path.equals(publicPath);
        });
    }
    
    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, String message, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = Map.of(
            "success", false,
            "message", message,
            "error", errorCode,
            "status", 401,
            "timestamp", System.currentTimeMillis()
        );
        
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes());
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
    
    public static class Config {
        private List<String> publicEndpoints;
        
        public List<String> getPublicEndpoints() {
            return publicEndpoints;
        }
        
        public void setPublicEndpoints(List<String> publicEndpoints) {
            this.publicEndpoints = publicEndpoints;
        }
    }
}
