package com.nexapay.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.promotion.common.R;
import com.nexapay.promotion.common.service.TokenService;
import com.nexapay.promotion.dto.channel.LoginDTO;
import com.nexapay.promotion.dto.channel.RegisterDTO;
import com.nexapay.promotion.dto.channel.SendVerificationCodeDTO;
import com.nexapay.promotion.entity.ChannelUser;

import com.nexapay.promotion.mapper.ChannelUserMapper;
import com.nexapay.promotion.mapper.VerificationCodeMapper;
import com.nexapay.promotion.service.AuthService;
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
public class AuthServiceImpl implements AuthService {

    private final ChannelUserMapper channelUserMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final EmailService emailService;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    private static final long VERIFICATION_CODE_EXPIRE_TIME = 5; // 5分钟过期
    private static final String TOKEN_KEY_PREFIX = "user_token:";  // 新增token前缀
    private static final long TOKEN_EXPIRE_TIME = 7;  // token 7天过期

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

        // 邮箱查重逻辑保持不变
        if (channelUserMapper.selectOne(new LambdaQueryWrapper<ChannelUser>()
                .eq(ChannelUser::getEmail, registerDTO.getEmail())) != null) {
            return R.error("邮箱已被注册");
        }

        // 创建用户
        ChannelUser channelUser = new ChannelUser();
        channelUser.setEmail(registerDTO.getEmail());
        channelUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        channelUser.setStatus(1);
        channelUser.setKycStatus(0);
        channelUser.setInviteCode(null);
        channelUser.setCreateTime(LocalDateTime.now());
        channelUser.setUpdateTime(LocalDateTime.now());

        channelUserMapper.insert(channelUser);
        redisTemplate.delete(key);  // 删除验证码
        String platform=registerDTO.getPlatform();

        String token = tokenService.createToken(channelUser.getId(),platform);  // 使用用户ID而不是email

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", channelUser.getEmail());
            put("kycStatus", channelUser.getKycStatus());
            put("inviteCode", channelUser.getInviteCode());
        }});

        return R.success(result);
    }


    @Override
    public R login(LoginDTO loginDTO) {
        ChannelUser channelUser = channelUserMapper.selectOne(
                new LambdaQueryWrapper<ChannelUser>()
                        .eq(ChannelUser::getEmail, loginDTO.getEmail())
        );

        if (channelUser == null) {
            return R.error("用户不存在");
        }

        if (channelUser.getStatus() != 1) {
            return R.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), channelUser.getPassword())) {
            return R.error("密码错误");
        }

        String platform=loginDTO.getPlatform();

        // 生成token并存储到Redis
        String token = tokenService.createToken(channelUser.getId(),platform);  // 使用用户ID而不是email

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", channelUser.getEmail());
            put("kycStatus", channelUser.getKycStatus());
            put("inviteCode", channelUser.getInviteCode());
        }});

        return R.success(result);
    }


}