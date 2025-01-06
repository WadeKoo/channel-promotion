package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.LoginDTO;
import com.nexapay.promotion.dto.RegisterDTO;
import com.nexapay.promotion.dto.SendVerificationCodeDTO;

public interface AuthService {
    R register(RegisterDTO registerDTO);
    R login(LoginDTO loginDTO);
    R sendVerificationCode(SendVerificationCodeDTO dto);
}
