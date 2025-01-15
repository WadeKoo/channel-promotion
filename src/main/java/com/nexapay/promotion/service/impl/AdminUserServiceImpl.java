package com.nexapay.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.promotion.common.R;
import com.nexapay.promotion.common.service.TokenService;
import com.nexapay.promotion.dto.admin.AdminLoginDTO;
import com.nexapay.promotion.dto.admin.AdminRegisterDTO;
import com.nexapay.promotion.dto.admin.AdminSendCodeDTO;
import com.nexapay.promotion.entity.AdminUser;
import com.nexapay.promotion.mapper.AdminUserMapper;
import com.nexapay.promotion.service.AdminUserService;
import com.nexapay.promotion.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VERIFICATION_CODE_KEY_PREFIX = "admin_verification_code:";
    private static final long VERIFICATION_CODE_EXPIRE_TIME = 5; // 5分钟过期
    private static final String PLATFORM_TYPE = "channel-admin";

    @Override
    public R sendVerificationCode(AdminSendCodeDTO dto) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = VERIFICATION_CODE_KEY_PREFIX + dto.getEmail();

        redisTemplate.opsForValue().set(key, code, VERIFICATION_CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        emailService.sendVerificationCode(dto.getEmail(), code);

        return R.success("验证码已发送");
    }

    @Override
    @Transactional
    public R register(AdminRegisterDTO registerDTO) {
        String key = VERIFICATION_CODE_KEY_PREFIX + registerDTO.getEmail();
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(registerDTO.getVerificationCode())) {
            return R.error("验证码无效或已过期");
        }

        if (adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getEmail, registerDTO.getEmail())) != null) {
            return R.error("邮箱已被注册");
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setEmail(registerDTO.getEmail());
        adminUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        adminUser.setStatus(1);
        adminUser.setRole("ADMIN");
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());

        adminUserMapper.insert(adminUser);
        redisTemplate.delete(key);

        String token = tokenService.createToken(adminUser.getId(), PLATFORM_TYPE);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", adminUser.getEmail());
            put("role", adminUser.getRole());
        }});

        return R.success(result);
    }

    @Override
    public R login(AdminLoginDTO loginDTO) {
        AdminUser adminUser = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getEmail, loginDTO.getEmail())
        );

        if (adminUser == null) {
            return R.error("管理员不存在");
        }

        if (adminUser.getStatus() != 1) {
            return R.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), adminUser.getPassword())) {
            return R.error("密码错误");
        }

        String token = tokenService.createToken(adminUser.getId(), PLATFORM_TYPE);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", adminUser.getEmail());
            put("role", adminUser.getRole());
        }});

        return R.success(result);
    }
}