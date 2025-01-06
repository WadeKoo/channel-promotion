package com.nexapay.promotion.controller;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.LoginDTO;
import com.nexapay.promotion.dto.RegisterDTO;
import com.nexapay.promotion.dto.SendVerificationCodeDTO;
import com.nexapay.promotion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public R register(@Validated @RequestBody RegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    @PostMapping("/login")
    public R login(@Validated @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    @PostMapping("/send-code")
    public R sendVerificationCode(@Validated @RequestBody SendVerificationCodeDTO dto) {
        return authService.sendVerificationCode(dto);
    }
}