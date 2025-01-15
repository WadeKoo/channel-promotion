package com.nexapay.agency.filter;

import com.nexapay.agency.common.model.TokenValidationResult;
import com.nexapay.agency.common.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_AUTH = "Authorization";
    private static final String PLATFORM_HEADER = "X-Platform";

    private static final String CHANNEL_PLATFORM = "agency";
    private static final String CHANNEL_ADMIN_PLATFORM = "agency-admin";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);
            String platform = getPlatformFromRequest(request);

            if (StringUtils.hasText(token)) {
                TokenValidationResult validationResult = tokenService.validateToken(token);

                if (validationResult.isValid() && platform.equals(validationResult.getPlatform())) {
                    String role = CHANNEL_PLATFORM.equals(platform) ? "ROLE_CHANNEL" : "ROLE_CHANNEL_ADMIN";
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(role)
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    validationResult.getUserId(),
                                    null,
                                    authorities
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }

        chain.doFilter(request, response);
    }

    private String getPlatformFromRequest(HttpServletRequest request) {
        String platform = request.getHeader(PLATFORM_HEADER);
        if (!StringUtils.hasText(platform)) {
            // 根据请求路径判断平台类型
            String requestPath = request.getRequestURI();
            if (requestPath.startsWith("/admin/")) {
                return CHANNEL_ADMIN_PLATFORM;
            } else {
                return CHANNEL_PLATFORM;
            }
        }
        return platform;
    }

    private String getTokenFromRequest(@NonNull HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTH);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}