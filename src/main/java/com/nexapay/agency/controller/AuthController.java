package com.nexapay.agency.controller;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.LoginDTO;
import com.nexapay.agency.dto.agency.RegisterDTO;
import com.nexapay.agency.dto.agency.ResetPasswordDTO;
import com.nexapay.agency.dto.agency.SendVerificationCodeDTO;
import com.nexapay.agency.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/auth")
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

    @GetMapping("/verify-email")
    public R verifyEmail(@RequestParam String email) {
        return authService.verifyEmail(email);
    }

    @PostMapping("/reset-password")
    public R resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        return authService.resetPassword(dto);
    }
}