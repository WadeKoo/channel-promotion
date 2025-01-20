package com.nexapay.agency.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexapay.agency.common.R;
import com.nexapay.agency.filter.JwtAuthenticationFilter;
import com.nexapay.agency.common.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenService tokenService;
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/auth/*",
            "/agency/promotion/**",// 认证相关接口
            "/admin/user/auth/*",         // 认证相关接口
            "/swagger-ui.html",      // Swagger UI
            "/swagger-resources/**",  // Swagger 资源
            "/v3/api-docs/**",       // OpenAPI
            "/webjars/**",           // Swagger UI webjars
            "/error"                 // 错误页面
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST.toArray(new String[0])).permitAll()
                        .requestMatchers("/agency/**").hasRole("CHANNEL")
                        .requestMatchers("/agency-admin/**").hasRole("CHANNEL_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(tokenService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            PrintWriter writer = response.getWriter();
                            writer.write(new ObjectMapper().writeValueAsString(R.error(40003, "请先登录")));
                            writer.flush();
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            PrintWriter writer = response.getWriter();
                            writer.write(new ObjectMapper().writeValueAsString(R.error(40004, "无访问权限")));
                            writer.flush();
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}