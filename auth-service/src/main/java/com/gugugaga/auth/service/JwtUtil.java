package com.gugugaga.auth.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.gugugaga.auth.entity.RefreshToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Autowired
    private UserDetailsService userDetailsService;

    // Use a stronger, longer secret key
    private final String SECRET_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkw";
    private final long EXP_TIME = 1000 * 60 * 60; // 1 hour
    private final long accessTokenExpiration = 1000 * 60 * 60; // 1 hour
    private final long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7; // 7 days

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails);
    }

    public String generateAccessTokenFromRefreshToken(RefreshToken refreshToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
        return generateAccessToken(userDetails);
    }
    
    // Keep your existing working generateToken method unchanged
    public String generateToken( UserDetails userDetails ) {
        System.out.println("Generating token for user: " + userDetails.getUsername());
        String token = Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt( new Date() )
            .expiration(new Date( System.currentTimeMillis() + EXP_TIME))
            .signWith(getSigningKey())
            .compact();
        System.out.println("Generated token: " + token);
        return token;
    }
    
    // Add new method for generating tokens with type
    public String generateTokenWithType(String username, String tokenType) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType);
        return createToken(claims, username, "ACCESS".equals(tokenType) ? accessTokenExpiration : refreshTokenExpiration);
    }
    
    // Add the createToken method that supports claims and custom expiration
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        System.out.println("Creating token with type for user: " + subject);
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    // Add method to extract token type
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    
    // Keep all your existing working methods unchanged
    public String extractUsername( String token ) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            System.out.println("Extracted username: " + username);
            return username;
        } catch (Exception e) {
            System.err.println("Error extracting username: " + e.getMessage());
            throw e;
        }
    }
    
    public boolean validateToken( String token, UserDetails userDetails ) {
        try {
            System.out.println("Validating token for user: " + userDetails.getUsername());
            final String username = extractUsername(token);
            boolean isValid = ( username.equals(userDetails.getUsername()) && !isTokenExpired(token) );
            System.out.println("Token validation result: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }
    
    // Add method to validate token with type checking
    public boolean validateTokenWithType(String token, UserDetails userDetails, String expectedType) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            boolean isValid = username.equals(userDetails.getUsername()) 
                            && !isTokenExpired(token) 
                            && expectedType.equals(tokenType);
            System.out.println("Token validation with type result: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating token with type: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isTokenExpired( String token ){
        return extractTokenExpiration(token).before( new Date());
    }
    
    private Date extractTokenExpiration( String token ) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            System.out.println("Parsing token: " + token.substring(0, Math.min(20, token.length())) + "...");
            return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            System.err.println("Error parsing token: " + e.getMessage());
            System.err.println("Secret key being used: " + SECRET_KEY.substring(0, 10) + "...");
            throw e;
        }
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
