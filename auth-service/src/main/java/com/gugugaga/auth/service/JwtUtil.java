package com.gugugaga.auth.service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    // Use a stronger, longer secret key
    private final String SECRET_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkw";
    private final long EXP_TIME = 1000 * 60 * 60; // 1 hour

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
