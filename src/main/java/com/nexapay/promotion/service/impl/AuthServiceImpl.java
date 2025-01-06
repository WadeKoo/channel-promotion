package com.nexapay.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.LoginDTO;
import com.nexapay.promotion.dto.RegisterDTO;
import com.nexapay.promotion.dto.SendVerificationCodeDTO;
import com.nexapay.promotion.entity.User;
import com.nexapay.promotion.entity.VerificationCode;
import com.nexapay.promotion.mapper.UserMapper;
import com.nexapay.promotion.mapper.VerificationCodeMapper;
import com.nexapay.promotion.service.AuthService;
import com.nexapay.promotion.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public R register(RegisterDTO registerDTO) {
        // 验证邮箱是否已注册
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, registerDTO.getEmail())
        );

        if (existingUser != null) {
            return R.error("邮箱已被注册");
        }

        // 验证验证码
        VerificationCode code = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getEmail, registerDTO.getEmail())
                        .eq(VerificationCode::getCode, registerDTO.getVerificationCode())
                        .eq(VerificationCode::getUsed, false)
        );

        if (code == null || code.getExpireTime().isBefore(LocalDateTime.now())) {
            return R.error("验证码无效或已过期");
        }

        // 创建用户
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 标记验证码已使用
        code.setUsed(true);
        verificationCodeMapper.updateById(code);

        return R.success("注册成功");
    }

    @Override
    public R login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, loginDTO.getEmail())
        );

        if (user == null) {
            return R.error("用户不存在");
        }

        if (user.getStatus() != 1) {
            return R.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return R.error("密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user);

        return R.success(token);
    }

    @Override
    public R sendVerificationCode(SendVerificationCodeDTO dto) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 保存验证码
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(dto.getEmail());
        verificationCode.setCode(code);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(false);
        verificationCode.setCreateTime(LocalDateTime.now());

        verificationCodeMapper.insert(verificationCode);

        // TODO: 发送邮件
        // 这里需要添加邮件发送的实现
        // 为了测试方便，先直接返回验证码
        return R.success(code);
    }
}