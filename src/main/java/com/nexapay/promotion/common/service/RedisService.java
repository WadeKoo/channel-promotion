package com.nexapay.promotion.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    public enum KeyPrefix {
        VERIFICATION_CODE("verification:", 5),     // 验证码 5分钟过期
        USER_TOKEN("token:", 7 * 24),             // token 7天过期
        USER_INFO("user:", 30 * 24);              // 用户信息 30天过期

        private final String prefix;
        private final long expireHours;

        KeyPrefix(String prefix, long expireHours) {
            this.prefix = prefix;
            this.expireHours = expireHours;
        }
    }

    private String getKey(KeyPrefix prefix, String key) {
        return applicationName + ":" + prefix.prefix + key;
    }

    public void set(KeyPrefix prefix, String key, Object value) {
        String fullKey = getKey(prefix, key);
        redisTemplate.opsForValue().set(fullKey, value, prefix.expireHours, TimeUnit.HOURS);
    }

    public Object get(KeyPrefix prefix, String key) {
        return redisTemplate.opsForValue().get(getKey(prefix, key));
    }

    public void delete(KeyPrefix prefix, String key) {
        redisTemplate.delete(getKey(prefix, key));
    }

    public boolean hasKey(KeyPrefix prefix, String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getKey(prefix, key)));
    }
}