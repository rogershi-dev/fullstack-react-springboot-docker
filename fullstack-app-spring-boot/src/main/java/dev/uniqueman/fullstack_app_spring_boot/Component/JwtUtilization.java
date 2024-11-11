package dev.uniqueman.fullstack_app_spring_boot.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtilization {
        
    @Value("${jwt.secret}")
    private String base64Secret;

    @Value("${app.session-expiration}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("username", user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build() 
                .parseClaimsJws(token) 
                .getBody(); 
    }

    public Duration getTokenRemainingDuration(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expirationDate = claims.getExpiration();
            long now = System.currentTimeMillis();
            long diff = expirationDate.getTime() -  now;
            return diff > 0 ? Duration.ofMillis(diff) : Duration.ZERO;

        } catch (JwtException | IllegalArgumentException e) {
            return Duration.ZERO;
        }
    }    

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

}
