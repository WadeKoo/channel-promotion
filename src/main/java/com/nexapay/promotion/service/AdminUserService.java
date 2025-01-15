package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.admin.AdminLoginDTO;
import com.nexapay.promotion.dto.admin.AdminRegisterDTO;
import com.nexapay.promotion.dto.admin.AdminSendCodeDTO;

public interface AdminUserService {
    R sendVerificationCode(AdminSendCodeDTO dto);
    R register(AdminRegisterDTO registerDTO);
    R login(AdminLoginDTO loginDTO);
}