package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.LoginDTO;
import com.nexapay.agency.dto.agency.RegisterDTO;
import com.nexapay.agency.dto.agency.SendVerificationCodeDTO;

public interface AuthService {
    R register(RegisterDTO registerDTO);
    R login(LoginDTO loginDTO);
    R sendVerificationCode(SendVerificationCodeDTO dto);
}
