package com.nexapay.promotion.common.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import com.nexapay.promotion.common.model.TokenValidationResult;

public interface TokenService {
    /**
     * 创建token
     * @param userId 用户ID
     * @return token字符串
     */
    @NonNull
    String createToken(@NonNull Long userId, @NonNull String platform);

    /**
     * 验证token并返回结果
     * @param token JWT token
     * @return TokenValidationResult 验证结果
     */
    @NonNull
    TokenValidationResult validateToken(@NonNull String token);

    /**
     * 使token失效
     * @param token JWT token
     */
    void invalidateToken(@NonNull String token);
}