package com.gugugaga.gateway.util;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public boolean isTokenExpired(String token) {
        return extractTokenExp(token).before(new Date());
    }
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
    
    public String extractTokenType( String token ){
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    private Date extractTokenExp( String token ) {
        return extractClaim(token, Claims::getExpiration);
    }
    private <T> T extractClaim( String token, Function<Claims, T> claimFunction ) {
        final Claims claim = extractAllClaims(token);
        return claimFunction.apply(claim);
    }
    private Claims extractAllClaims( String token ) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }
    private SecretKey getSigningKey(){
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
