package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.agency.common.R;
import com.nexapay.agency.common.service.TokenService;
import com.nexapay.agency.dto.agency.LoginDTO;
import com.nexapay.agency.dto.agency.RegisterDTO;
import com.nexapay.agency.dto.agency.ResetPasswordDTO;
import com.nexapay.agency.dto.agency.SendVerificationCodeDTO;
import com.nexapay.agency.entity.AgencyUser;

import com.nexapay.agency.mapper.AgencyUserMapper;
import com.nexapay.agency.service.AuthService;
import com.nexapay.agency.service.EmailService;
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

    private final AgencyUserMapper agencyUserMapper;
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
        // 根据类型验证邮箱
        if ("register".equals(dto.getType())) {
            // 注册时验证邮箱是否已存在
            if (agencyUserMapper.selectOne(new LambdaQueryWrapper<AgencyUser>()
                    .eq(AgencyUser::getEmail, dto.getEmail())) != null) {
                return R.error("邮箱已被注册");
            }
        } else if ("reset".equals(dto.getType())) {
            // 重置密码时验证邮箱是否存在
            if (agencyUserMapper.selectOne(new LambdaQueryWrapper<AgencyUser>()
                    .eq(AgencyUser::getEmail, dto.getEmail())) == null) {
                return R.error("邮箱不存在");
            }
        }

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
        if (agencyUserMapper.selectOne(new LambdaQueryWrapper<AgencyUser>()
                .eq(AgencyUser::getEmail, registerDTO.getEmail())) != null) {
            return R.error("邮箱已被注册");
        }

        // 创建用户
        AgencyUser agencyUser = new AgencyUser();
        agencyUser.setEmail(registerDTO.getEmail());
        agencyUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        agencyUser.setStatus(1);
        agencyUser.setKycStatus(0);
        agencyUser.setInviteCode(null);
        agencyUser.setCreateTime(LocalDateTime.now());
        agencyUser.setUpdateTime(LocalDateTime.now());

        agencyUserMapper.insert(agencyUser);
        redisTemplate.delete(key);  // 删除验证码
        String platform=registerDTO.getPlatform();

        String token = tokenService.createToken(agencyUser.getId(),platform);  // 使用用户ID而不是email

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", agencyUser.getEmail());
            put("kycStatus", agencyUser.getKycStatus());
            put("inviteCode", agencyUser.getInviteCode());
        }});

        return R.success(result);
    }


    @Override
    public R login(LoginDTO loginDTO) {
        AgencyUser agencyUser = agencyUserMapper.selectOne(
                new LambdaQueryWrapper<AgencyUser>()
                        .eq(AgencyUser::getEmail, loginDTO.getEmail())
        );

        if (agencyUser == null) {
            return R.error("用户不存在");
        }

        if (agencyUser.getStatus() != 1) {
            return R.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), agencyUser.getPassword())) {
            return R.error("密码错误");
        }

        String platform=loginDTO.getPlatform();

        // 生成token并存储到Redis
        String token = tokenService.createToken(agencyUser.getId(),platform);  // 使用用户ID而不是email

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", new HashMap<String, Object>() {{
            put("email", agencyUser.getEmail());
            put("kycStatus", agencyUser.getKycStatus());
            put("inviteCode", agencyUser.getInviteCode());
        }});

        return R.success(result);
    }

    @Override
    public R verifyEmail(String email) {
        AgencyUser user = agencyUserMapper.selectOne(
                new LambdaQueryWrapper<AgencyUser>()
                        .eq(AgencyUser::getEmail, email)
        );

        if (user == null) {
            return R.error("邮箱不存在");
        }

        return R.success("邮箱验证成功");
    }

    @Override
    @Transactional
    public R resetPassword(ResetPasswordDTO dto) {
        // 验证用户是否存在
        AgencyUser user = agencyUserMapper.selectOne(
                new LambdaQueryWrapper<AgencyUser>()
                        .eq(AgencyUser::getEmail, dto.getEmail())
        );

        if (user == null) {
            return R.error("用户不存在");
        }

        // 验证验证码
        String key = VERIFICATION_CODE_KEY_PREFIX + dto.getEmail();
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(dto.getVerificationCode())) {
            return R.error("验证码无效或已过期");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        agencyUserMapper.updateById(user);

        // 删除验证码
        redisTemplate.delete(key);

        return R.success("密码重置成功");
    }



}