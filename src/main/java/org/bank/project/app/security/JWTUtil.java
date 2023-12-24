package org.bank.project.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.bank.project.app.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    @Value("${webflux.jwt.secret}")
    private String secret;

    @Value("${webflux.jwt.expiration}")
    private String expirationTime;

    @Value("${webflux.jwt.refresh.expiration}")
    private String refreshExpirationTime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        return doGenerateToken(claims, user.getUsername(), expirationTime);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoles());
        return doGenerateToken(claims, user.getUsername(), refreshExpirationTime);
    }

    public String generateTokenFromRefreshToken(String token) {
        String username = getUsernameFromToken(token);
        Claims claims = getAllClaimsFromToken(token);
        return doGenerateToken(claims, username, expirationTime);
    }

    private String doGenerateToken(Map<String, Object> claims, String username, String expiry) {
        long expirationTimeLong = Long.parseLong(expiry);
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

        return Jwts.builder().claims(claims).subject(username).issuedAt(createdDate).expiration(expirationDate)
                .signWith(key).compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
