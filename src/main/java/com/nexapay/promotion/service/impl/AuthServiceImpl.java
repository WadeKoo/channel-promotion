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
import com.nexapay.promotion.service.EmailService;
import com.nexapay.promotion.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    private static final long VERIFICATION_CODE_EXPIRE_TIME = 5; // 5分钟过期

    @Override
    public R sendVerificationCode(SendVerificationCodeDTO dto) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = VERIFICATION_CODE_KEY_PREFIX + dto.getEmail();

        redisTemplate.opsForValue().set(key, code, VERIFICATION_CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        emailService.sendVerificationCode(dto.getEmail(), code);

        return R.success("验证码已发送");
    }

    @Override
    @Transactional
    public R register(RegisterDTO registerDTO) {
        String key = VERIFICATION_CODE_KEY_PREFIX + registerDTO.getEmail();
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(registerDTO.getVerificationCode())) {
            return R.error("验证码无效或已过期");
        }

        // 验证邮箱
        if (userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, registerDTO.getEmail())) != null) {
            return R.error("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        redisTemplate.delete(key);

        return R.success(jwtUtil.generateToken(user));
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


}