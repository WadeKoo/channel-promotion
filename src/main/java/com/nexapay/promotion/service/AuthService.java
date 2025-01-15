package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.channel.LoginDTO;
import com.nexapay.promotion.dto.channel.RegisterDTO;
import com.nexapay.promotion.dto.channel.SendVerificationCodeDTO;

public interface AuthService {
    R register(RegisterDTO registerDTO);
    R login(LoginDTO loginDTO);
    R sendVerificationCode(SendVerificationCodeDTO dto);
}
