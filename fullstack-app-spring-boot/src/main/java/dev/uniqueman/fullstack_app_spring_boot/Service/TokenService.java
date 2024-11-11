package dev.uniqueman.fullstack_app_spring_boot.Service;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import dev.uniqueman.fullstack_app_spring_boot.Exception.TokenManagementException;

@Service
public class TokenService {
    
    private static final String USER_TOKENS_PREFIX = "user_tokens:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addToken(String username, String token, int limit, Duration tokenExpiry) {
        String key = USER_TOKENS_PREFIX + username;
        long currentTimeMillis = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(key, token, currentTimeMillis);

        redisTemplate.expire(key, tokenExpiry.getSeconds(), TimeUnit.SECONDS);

        Long size = redisTemplate.opsForZSet().zCard(key);

        if (size != null && size > limit) {
            Set<String> tokensToRemove = redisTemplate.opsForZSet().range(key, 0, size - limit - 1);
            if (tokensToRemove != null) {
                for (String oldToken : tokensToRemove) {
                    removeToken(username, oldToken);
                    addToBlacklist(oldToken, tokenExpiry);
                }
            }
        }

    }

    public void removeToken(String username, String token) {
        try {
            String key = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForZSet().remove(key, token);
            
        } catch (Exception e) {
            throw new TokenManagementException("Token removal failed");
        }
    }

    public boolean isTokenActive(String username, String token) {
        String key = USER_TOKENS_PREFIX + username;
        return redisTemplate.opsForZSet().score(key, token) != null;
    }

    public void addToBlacklist(String token, Duration tokenExpiry) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", tokenExpiry.getSeconds(), TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }    
}
