package com.gugugaga.auth.filter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gugugaga.auth.dto.ErrorResponse;
import com.gugugaga.auth.service.JwtUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Set<String>> PUBLIC_ENDPOINTS = Map.of(
        "/api/auth/login", Set.of("POST"),
        "/api/auth/register", Set.of("POST"),
        "/api/auth/refresh", Set.of("GET", "POST") // support both if needed
    );


    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        
        System.out.println("JWT Filter - Processing request: " + request.getRequestURI());
        System.out.println("JWT Filter - Authorization header: " + authHeader);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            handleAuthenticationError(request, response, "Missing or invalid Authorization header");
            return;
        }
        
        jwt = authHeader.substring(7);
        System.out.println("JWT Filter - Extracted JWT: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
        
        try {
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JWT Filter - Extracted username: " + username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter - Loaded user details for: " + userDetails.getUsername());
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    System.out.println("JWT Filter - Token is valid, setting authentication");
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    System.out.println("JWT Filter - Authentication set successfully");
                } else {
                    handleAuthenticationError(request, response, "Invalid or expired token");
                    return;
                }
            }
        } catch (ExpiredJwtException e) {
            System.err.println("JWT Filter - Token expired: " + e.getMessage());
            handleAuthenticationError(request, response, "Token has expired");
            return;
        } catch (MalformedJwtException e) {
            System.err.println("JWT Filter - Malformed token: " + e.getMessage());
            handleAuthenticationError(request, response, "Malformed JWT token");
            return;
        } catch (SignatureException e) {
            System.err.println("JWT Filter - Invalid signature: " + e.getMessage());
            handleAuthenticationError(request, response, "Invalid JWT signature");
            return;
        } catch (JwtException e) {
            System.err.println("JWT Filter - JWT error: " + e.getMessage());
            handleAuthenticationError(request, response, "JWT processing error: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("JWT Filter - General error: " + e.getMessage());
            handleAuthenticationError(request, response, "Token validation failed");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path   = request.getServletPath();
        String method = request.getMethod();

        return PUBLIC_ENDPOINTS.containsKey(path) && PUBLIC_ENDPOINTS.get(path).contains(method);
    }

    private void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        Map<String, String> errors = Map.of("error", message);
        objectMapper.registerModule( new JavaTimeModule());
        objectMapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse2 = new ErrorResponse(
            Instant.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication failed",
            "Unauthorized",
            request.getRequestURI(),
            errors
        );
        String jsonResponse = objectMapper.writeValueAsString(errorResponse2);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
        
}
