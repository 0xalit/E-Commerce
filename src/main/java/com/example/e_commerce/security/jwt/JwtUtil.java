package com.example.e_commerce.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final Key key;
    private final String issuer;
    @Getter
    private final long expirationMillis;
    private static final String ROLES_CLAIM = "roles";

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expiration-minutes}") long expirationMinutes) {

        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.expirationMillis = expirationMinutes * 60L * 1000L;
    }

    public String generateToken(String email, List<String> roles) {
        long now = System.currentTimeMillis();
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(ROLES_CLAIM, roles);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        Object raw = claims.get(ROLES_CLAIM);
        if (raw instanceof List<?>) {
            return ((List<?>) raw).stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
