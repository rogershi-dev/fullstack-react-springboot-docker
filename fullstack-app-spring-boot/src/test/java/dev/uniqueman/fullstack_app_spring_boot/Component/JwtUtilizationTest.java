package dev.uniqueman.fullstack_app_spring_boot.Component;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import dev.uniqueman.fullstack_app_spring_boot.Entity.Role;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtilizationTest {
    
    private JwtUtilization jwtUtil;
    private final String base64Secret = Base64.getEncoder().encodeToString("mySuperSecretKeyForJwtGeneration123!".getBytes());
    private final long expiration = 3600000L; 
    private User user;

    @BeforeEach 
    public void setUp() {

        jwtUtil = new JwtUtilization();

        ReflectionTestUtils.setField(jwtUtil, "base64Secret", base64Secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        jwtUtil.init();
        user = new User();
        user.setId(123456789L);
        user.setUsername("TestUsername");
        user.setHashedPassword("HashedPassword");
        user.setRole(Role.ADMIN);
    }

    @Test 
    public void testGenerateToken() {
        String token = jwtUtil.generateToken(user);
        assertNotNull(token, "Generated token should not be null");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret)))
                .build() 
                .parseClaimsJws(token) 
                .getBody();

        assertEquals(user.getId().toString(), claims.getSubject(), "Subject should match user id");
        assertEquals(user.getUsername(), claims.get("username", String.class), "Username claim should match");
        assertNotNull(claims.getIssuedAt(), "issuedAt should not be null");
        assertNotNull(claims.getExpiration(), "Expiration should not be null");
    }

}
