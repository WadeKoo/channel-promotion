package com.nexapay.promotion.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nexapay.promotion.exception.BusinessException;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(40003, "用户未登录");
        }

        // 这里的实现需要根据你的用户认证信息具体结构来调整
        // 这里假设Principal是存储了用户ID的对象
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception e) {
            throw new BusinessException(40003, "获取用户信息失败");
        }
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}