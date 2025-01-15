package com.nexapay.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.admin.ChannelCommissionConfigRequest;
import com.nexapay.promotion.dto.admin.ChannelListDTO;
import com.nexapay.promotion.dto.channel.CompanyInfoDTO;
import com.nexapay.promotion.dto.channel.PersonalInfoDTO;
import com.nexapay.promotion.dto.admin.MerchantVerificationListDTO;
import com.nexapay.promotion.entity.ChannelCommissionConfig;
import com.nexapay.promotion.entity.ChannelUser;
import com.nexapay.promotion.entity.UserKYCVerification;
import com.nexapay.promotion.mapper.ChannelCommissionConfigMapper;
import com.nexapay.promotion.mapper.UserKYCVerificationMapper;
import com.nexapay.promotion.mapper.ChannelUserMapper;
import com.nexapay.promotion.service.ChannelManagementService;
import com.nexapay.promotion.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelManagementServiceImpl implements ChannelManagementService {

    private final UserKYCVerificationMapper verificationMapper;
    private final ChannelCommissionConfigMapper channelCommissionConfigMapper;
    private final ChannelUserMapper channelUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    public R getPendingVerifications(Integer page, Integer size) {
        // Create pagination object
        Page<UserKYCVerification> pageParam = new Page<>(page, size);

        // Query conditions - put submitted status first, then order by creation date
        LambdaQueryWrapper<UserKYCVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .last("ORDER BY FIELD(status, 'submitted', 'approved', 'rejected') ASC, created_at DESC");

        // Execute query
        Page<UserKYCVerification> verificationPage = verificationMapper.selectPage(pageParam, queryWrapper);

        // Transform results
        List<MerchantVerificationListDTO> dtoList = new ArrayList<>();
        for (UserKYCVerification verification : verificationPage.getRecords()) {
            MerchantVerificationListDTO dto = new MerchantVerificationListDTO();
            dto.setId(verification.getId());
            dto.setType(verification.getType());
            dto.setCreatedAt(verification.getCreatedAt());
            dto.setStatus(verification.getStatus());

            // Get user email
            ChannelUser user = channelUserMapper.selectById(verification.getUserId());
            if (user != null) {
                dto.setEmail(user.getEmail());
            }

            // Get name based on type
            try {
                if ("personal".equals(verification.getType())) {
                    PersonalInfoDTO personalInfo = objectMapper.readValue(
                            verification.getPersonalInfo(), PersonalInfoDTO.class);
                    dto.setName(personalInfo.getName());
                } else {
                    CompanyInfoDTO companyInfo = objectMapper.readValue(
                            verification.getCompanyInfo(), CompanyInfoDTO.class);
                    dto.setName(companyInfo.getCompanyName());
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON for verification {}: {}",
                        verification.getId(), e.getMessage());
                continue;
            }

            dtoList.add(dto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", verificationPage.getTotal());
        result.put("pages", verificationPage.getPages());
        result.put("list", dtoList);

        return R.success(result);
    }

    @Override
    public R getVerificationDetail(String id) {
        // Get verification record
        UserKYCVerification verification = verificationMapper.selectById(id);
        if (verification == null) {
            return R.error("Verification not found");
        }

        // Get user information
        ChannelUser user = channelUserMapper.selectById(verification.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", verification.getId());
        result.put("type", verification.getType());
        result.put("status", verification.getStatus());
        result.put("rejectReason", verification.getRejectReason());
        result.put("createdAt", verification.getCreatedAt());
        result.put("updatedAt", verification.getUpdatedAt());
        result.put("submittedAt", verification.getSubmittedAt());
        result.put("approvedAt", verification.getApprovedAt());

        if (user != null) {
            result.put("email", user.getEmail());
        }

        try {
            // Parse and add personal/company info based on type
            if ("personal".equals(verification.getType())) {
                PersonalInfoDTO personalInfo = objectMapper.readValue(
                        verification.getPersonalInfo(), PersonalInfoDTO.class);
                result.put("personalInfo", personalInfo);
            } else {
                CompanyInfoDTO companyInfo = objectMapper.readValue(
                        verification.getCompanyInfo(), CompanyInfoDTO.class);
                result.put("companyInfo", companyInfo);
            }

            // Parse and add other JSON fields if they exist
            if (verification.getBankInfo() != null) {
                result.put("bankInfo", objectMapper.readTree(verification.getBankInfo()));
            }

            if (verification.getDocuments() != null) {
                result.put("documents", objectMapper.readTree(verification.getDocuments()));
            }

            if (verification.getAgreementInfo() != null) {
                result.put("agreementInfo", objectMapper.readTree(verification.getAgreementInfo()));
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON for verification {}: {}", id, e.getMessage());
            return R.error("Error parsing verification data");
        }

        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R configCommission(ChannelCommissionConfigRequest request) {
        // 检查渠道商是否存在
        ChannelUser channelUser = channelUserMapper.selectById(request.getChannelUserId());
        if (channelUser == null) {
            return R.error("渠道商不存在");
        }

        // 查询是否已有配置
        LambdaQueryWrapper<ChannelCommissionConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChannelCommissionConfig::getChannelUserId, request.getChannelUserId())
                .eq(ChannelCommissionConfig::getStatus, 1);
        ChannelCommissionConfig config = channelCommissionConfigMapper.selectOne(queryWrapper);

        LocalDateTime now = LocalDateTime.now();

        if (config == null) {
            // 新增配置
            config = new ChannelCommissionConfig();
            config.setChannelUserId(request.getChannelUserId());
            config.setCommissionRate(request.getCommissionRate());
            config.setFirstOrderBonus(request.getFirstOrderBonus());
            config.setStatus(1);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setCreateBy(SecurityUtils.getCurrentUserId()); // 假设有获取当前登录用户的工具类
            config.setUpdateBy(SecurityUtils.getCurrentUserId());
            channelCommissionConfigMapper.insert(config);
        } else {
            // 更新配置
            config.setCommissionRate(request.getCommissionRate());
            config.setFirstOrderBonus(request.getFirstOrderBonus());
            config.setUpdateTime(now);
            config.setUpdateBy(SecurityUtils.getCurrentUserId());
            channelCommissionConfigMapper.updateById(config);
        }

        return R.success("配置成功");
    }

    @Override
    public R getChannelList(Integer page, Integer size) {
        // Create pagination object
        Page<ChannelUser> pageParam = new Page<>(page, size);

        // Query conditions
        LambdaQueryWrapper<ChannelUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ChannelUser::getCreateTime);

        // Execute query
        Page<ChannelUser> userPage = channelUserMapper.selectPage(pageParam, queryWrapper);

        // Transform results
        List<ChannelListDTO> dtoList = new ArrayList<>();
        for (ChannelUser user : userPage.getRecords()) {
            ChannelListDTO dto = new ChannelListDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setStatus(user.getStatus());
            dto.setKycStatus(user.getKycStatus());
            dto.setCreateTime(user.getCreateTime());

            // Get commission config
            LambdaQueryWrapper<ChannelCommissionConfig> configWrapper = new LambdaQueryWrapper<>();
            configWrapper.eq(ChannelCommissionConfig::getChannelUserId, user.getId())
                    .eq(ChannelCommissionConfig::getStatus, 1);
            ChannelCommissionConfig config = channelCommissionConfigMapper.selectOne(configWrapper);

            if (config != null) {
                dto.setCommissionRate(config.getCommissionRate());
                dto.setFirstOrderBonus(config.getFirstOrderBonus());
            }

            // Get KYC information
            LambdaQueryWrapper<UserKYCVerification> kycWrapper = new LambdaQueryWrapper<>();
            kycWrapper.eq(UserKYCVerification::getUserId, user.getId())
                    .orderByDesc(UserKYCVerification::getCreatedAt)
                    .last("LIMIT 1");
            UserKYCVerification verification = verificationMapper.selectOne(kycWrapper);

            if (verification != null) {
                dto.setType(verification.getType());
                try {
                    if ("personal".equals(verification.getType())) {
                        PersonalInfoDTO personalInfo = objectMapper.readValue(
                                verification.getPersonalInfo(), PersonalInfoDTO.class);
                        dto.setName(personalInfo.getName());
                        dto.setRegion(personalInfo.getCountry());
                    } else {
                        CompanyInfoDTO companyInfo = objectMapper.readValue(
                                verification.getCompanyInfo(), CompanyInfoDTO.class);
                        dto.setName(companyInfo.getCompanyName());
                        dto.setRegion(companyInfo.getCountry());
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing JSON for user {}: {}", user.getId(), e.getMessage());
                }
            }

            dtoList.add(dto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", userPage.getTotal());
        result.put("pages", userPage.getPages());
        result.put("list", dtoList);

        return R.success(result);
    }
}