package com.nexapay.promotion.common.service.impl;

import com.nexapay.promotion.common.model.TokenValidationResult;
import com.nexapay.promotion.common.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret:d6uUmNuE7kYpG5jW8vF3qH9nM2cX4tA5rB1sL7zQ9wK}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private static final String TOKEN_KEY_PREFIX = "user_token:";
    private static final long REDIS_TOKEN_EXPIRE_DAYS = 7;

    private static final String PLATFORM_CLAIM = "platform";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    @NonNull
    public String createToken(@NonNull Long userId, @NonNull String platform) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(PLATFORM_CLAIM, platform)  // 使用传入的platform参数
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // 将token存入Redis
        String redisKey = TOKEN_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(redisKey, userId, REDIS_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        return token;
    }

    @Override
    @NonNull
    public TokenValidationResult validateToken(@NonNull String token) {
        try {
            // 验证JWT的签名和过期时间
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 检查Redis中是否存在该token
            String redisKey = TOKEN_KEY_PREFIX + token;
            Object userId = redisTemplate.opsForValue().get(redisKey);

            if (userId != null) {
                String platform = claims.get(PLATFORM_CLAIM, String.class);
                return new TokenValidationResult(
                        Long.parseLong(claims.getSubject()),
                        true,
                        platform
                );
            }
        } catch (ExpiredJwtException e) {
            log.error("Token expired", e);
            // 清除Redis中的过期token
            invalidateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
        }
        return TokenValidationResult.invalid();
    }

    @Override
    public void invalidateToken(@NonNull String token) {
        String redisKey = TOKEN_KEY_PREFIX + token;
        redisTemplate.delete(redisKey);
    }
}