package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AdminLoginDTO;
import com.nexapay.agency.dto.admin.AdminRegisterDTO;
import com.nexapay.agency.dto.admin.AdminSendCodeDTO;

public interface AdminUserService {
    R sendVerificationCode(AdminSendCodeDTO dto);
    R register(AdminRegisterDTO registerDTO);
    R login(AdminLoginDTO loginDTO);
}