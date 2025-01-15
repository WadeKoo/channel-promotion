package com.nexapay.promotion.controller.admin;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.admin.AdminLoginDTO;
import com.nexapay.promotion.dto.admin.AdminRegisterDTO;
import com.nexapay.promotion.dto.admin.AdminSendCodeDTO;
import com.nexapay.promotion.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserService adminUserService;

    @PostMapping("/register")
    public R register(@Validated @RequestBody AdminRegisterDTO registerDTO) {
        return adminUserService.register(registerDTO);
    }

    @PostMapping("/login")
    public R login(@Validated @RequestBody AdminLoginDTO loginDTO) {
        return adminUserService.login(loginDTO);
    }

    @PostMapping("/send-code")
    public R sendVerificationCode(@Validated @RequestBody AdminSendCodeDTO dto) {
        return adminUserService.sendVerificationCode(dto);
    }
}