package com.iuh.WiseOwlEnglish_Backend.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import io.jsonwebtoken.Claims;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret; // đặt trong application.yml hoặc ENV

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

//    public String generateToken(UserDetails user, Map<String,Object> claims, Duration ttl) {
//        Instant now = Instant.now();
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(user.getUsername())
//                .setIssuedAt(Date.from(now))
//                .setExpiration(Date.from(now.plus(ttl)))
//                .signWith(signingKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
    /** subject = userId (String) */
    public String generateToken(String subject, Map<String,Object> claims, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // 👈 userId string
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

//    public boolean isValid(String token, UserDetails user) {
//        try {
//            final String sub = extractUsername(token);
//            return sub.equals(user.getUsername()) && !isExpired(token);
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
        public boolean isValid(String token) {
            try {
                return !isExpired(token);
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        }


    public boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public Long extractUserId(String token) {
        String sub = parseClaims(token).getSubject();
        if (sub == null) return null;
        return Long.valueOf(sub);
    }


    public String extractType(String token) {
        Object t = parseClaims(token).get("typ");
        return t == null ? null : t.toString();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }

}
