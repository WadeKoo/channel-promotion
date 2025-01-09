package com.nexapay.promotion.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface TokenService {
    /**
     * 创建token
     * @param userId 用户ID
     * @return token字符串
     */
    @NonNull
    String createToken(@NonNull Long userId);

    /**
     * 验证token并返回用户ID
     * @param token JWT token
     * @return 用户ID，如果token无效则返回null
     */
    @Nullable
    Long validateToken(@NonNull String token);
}
