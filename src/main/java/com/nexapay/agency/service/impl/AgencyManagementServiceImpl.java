package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.*;
import com.nexapay.agency.dto.agency.AgreementInfoDTO;
import com.nexapay.agency.dto.agency.CompanyInfoDTO;
import com.nexapay.agency.dto.agency.PersonalInfoDTO;
import com.nexapay.agency.entity.AgencyCommissionConfig;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.entity.AgencyKyc;
import com.nexapay.agency.mapper.AgencyCommissionConfigMapper;
import com.nexapay.agency.mapper.AgencyKycMapper;
import com.nexapay.agency.mapper.AgencyUserMapper;
import com.nexapay.agency.service.AgencyManagementService;
import com.nexapay.agency.service.EmailService;
import com.nexapay.agency.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyManagementServiceImpl implements AgencyManagementService {

    private final AgencyKycMapper agencyKycMapper;
    private final EmailService emailService;
    private final AgencyCommissionConfigMapper agencyCommissionConfigMapper;
    private final AgencyUserMapper agencyUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    public R getKycList(Integer page, Integer size) {
        // Create pagination object
        Page<AgencyKyc> pageParam = new Page<>(page, size);

        // Query conditions - put submitted status first, then order by creation date
        LambdaQueryWrapper<AgencyKyc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.last("ORDER BY FIELD(status, 'submitted', 'approved', 'rejected') ASC, created_at DESC");

        // Execute query
        Page<AgencyKyc> kycPage = agencyKycMapper.selectPage(pageParam, queryWrapper);

        // Transform results
        List<AgencyKycListDTO> dtoList = new ArrayList<>();
        for (AgencyKyc kyc : kycPage.getRecords()) {
            try {
                AgencyKycListDTO dto = new AgencyKycListDTO();
                dto.setId(kyc.getId());
                dto.setType(kyc.getType());
                dto.setCreatedAt(kyc.getCreatedAt());
                dto.setStatus(kyc.getStatus());

                // Get user email with null check
                if (kyc.getUserId() != null) {
                    AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());
                    if (user != null) {
                        dto.setEmail(user.getEmail());
                    }
                }

                // Get name based on type with null checks
                if ("personal".equals(kyc.getType()) && kyc.getPersonalInfo() != null) {
                    PersonalInfoDTO personalInfo = objectMapper.readValue(
                            kyc.getPersonalInfo(), PersonalInfoDTO.class);
                    if (personalInfo != null && personalInfo.getName() != null) {
                        dto.setName(personalInfo.getName());
                    }
                } else if ("company".equals(kyc.getType()) && kyc.getCompanyInfo() != null) {
                    CompanyInfoDTO companyInfo = objectMapper.readValue(
                            kyc.getCompanyInfo(), CompanyInfoDTO.class);
                    if (companyInfo != null && companyInfo.getCompanyName() != null) {
                        dto.setName(companyInfo.getCompanyName());
                    }
                }

                dtoList.add(dto);
            } catch (JsonProcessingException e) {
                // Log error but continue processing other records
                log.error("Error parsing JSON for kyc {}: {}", kyc.getId(), e.getMessage());
                continue;
            } catch (Exception e) {
                // Log any other unexpected errors but continue processing
                log.error("Unexpected error processing kyc {}: {}", kyc.getId(), e.getMessage());
                continue;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", kycPage.getTotal());
        result.put("pages", kycPage.getPages());
        result.put("list", dtoList);

        return R.success(result);
    }

    @Override
    public R getKycDetail(String id) {
        // Get kyc record
        AgencyKyc kyc = agencyKycMapper.selectById(id);
        if (kyc == null) {
            return R.error("Kyc not found");
        }

        // Get user information
        AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", kyc.getId());
        result.put("type", kyc.getType());
        result.put("status", kyc.getStatus());
        result.put("rejectReason", kyc.getRejectReason());
        result.put("createdAt", kyc.getCreatedAt());
        result.put("updatedAt", kyc.getUpdatedAt());
        result.put("submittedAt", kyc.getSubmittedAt());
        result.put("approvedAt", kyc.getApprovedAt());

        if (user != null) {
            result.put("email", user.getEmail());
        }

        try {
            // Parse and add personal/company info based on type
            if ("personal".equals(kyc.getType())) {
                PersonalInfoDTO personalInfo = objectMapper.readValue(
                        kyc.getPersonalInfo(), PersonalInfoDTO.class);
                result.put("personalInfo", personalInfo);
            } else {
                CompanyInfoDTO companyInfo = objectMapper.readValue(
                        kyc.getCompanyInfo(), CompanyInfoDTO.class);
                result.put("companyInfo", companyInfo);
            }

            // Parse and add other JSON fields if they exist
            if (kyc.getBankInfo() != null) {
                result.put("bankInfo", objectMapper.readTree(kyc.getBankInfo()));
            }

            if (kyc.getDocuments() != null) {
                result.put("documents", objectMapper.readTree(kyc.getDocuments()));
            }

            if (kyc.getAgreementInfo() != null) {
                result.put("agreementInfo", objectMapper.readTree(kyc.getAgreementInfo()));
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON for kyc {}: {}", id, e.getMessage());
            return R.error("Error parsing kyc data");
        }

        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R configCommission(AgencyCommissionConfigRequest request) {
        // 检查渠道商是否存在
        AgencyUser agencyUser = agencyUserMapper.selectById(request.getAgencyUserId());
        if (agencyUser == null) {
            return R.error("渠道商不存在");
        }

        // 查询是否已有配置
        LambdaQueryWrapper<AgencyCommissionConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgencyCommissionConfig::getAgencyUserId, request.getAgencyUserId())
                .eq(AgencyCommissionConfig::getStatus, 1);
        AgencyCommissionConfig config = agencyCommissionConfigMapper.selectOne(queryWrapper);

        LocalDateTime now = LocalDateTime.now();

        if (config == null) {
            // 新增配置
            config = new AgencyCommissionConfig();
            config.setAgencyUserId(request.getAgencyUserId());
            config.setCommissionRate(request.getCommissionRate());
            config.setFirstOrderBonus(request.getFirstOrderBonus());
            config.setStatus(1);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setCreateBy(SecurityUtils.getCurrentUserId()); // 假设有获取当前登录用户的工具类
            config.setUpdateBy(SecurityUtils.getCurrentUserId());
            agencyCommissionConfigMapper.insert(config);
        } else {
            // 更新配置
            config.setCommissionRate(request.getCommissionRate());
            config.setFirstOrderBonus(request.getFirstOrderBonus());
            config.setUpdateTime(now);
            config.setUpdateBy(SecurityUtils.getCurrentUserId());
            agencyCommissionConfigMapper.updateById(config);
        }

        return R.success("配置成功");
    }

    @Override
    public R getAgencyList(Integer page, Integer size) {
        // Parameter validation
        page = (page == null || page < 1) ? 1 : page;
        size = (size == null || size < 1) ? 10 : size;

        // Create pagination object
        Page<AgencyUser> pageParam = new Page<>(page, size);

        // Query conditions
        LambdaQueryWrapper<AgencyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AgencyUser::getCreateTime);

        // Execute query
        Page<AgencyUser> userPage = agencyUserMapper.selectPage(pageParam, queryWrapper);

        // Transform results
        List<AgencyListDTO> dtoList = new ArrayList<>();
        for (AgencyUser user : userPage.getRecords()) {
            AgencyListDTO dto = new AgencyListDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setStatus(user.getStatus());
            dto.setKycStatus(user.getKycStatus());
            dto.setCreateTime(user.getCreateTime());

            // Get commission configuration
            LambdaQueryWrapper<AgencyCommissionConfig> configWrapper = new LambdaQueryWrapper<>();
            configWrapper.eq(AgencyCommissionConfig::getAgencyUserId, user.getId())
                    .eq(AgencyCommissionConfig::getStatus, 1);
            AgencyCommissionConfig config = agencyCommissionConfigMapper.selectOne(configWrapper);

            if (config != null) {
                dto.setCommissionRate(config.getCommissionRate());
                dto.setFirstOrderBonus(config.getFirstOrderBonus());
            }

            // Get KYC information
            LambdaQueryWrapper<AgencyKyc> kycWrapper = new LambdaQueryWrapper<>();
            kycWrapper.eq(AgencyKyc::getUserId, user.getId())
                    .orderByDesc(AgencyKyc::getCreatedAt)
                    .last("LIMIT 1");
            AgencyKyc kyc = agencyKycMapper.selectOne(kycWrapper);

            if (kyc != null) {
                dto.setType(kyc.getType());
                try {
                    // Add null checks for JSON content
                    if ("personal".equals(kyc.getType()) && kyc.getPersonalInfo() != null) {
                        PersonalInfoDTO personalInfo = objectMapper.readValue(
                                kyc.getPersonalInfo(), PersonalInfoDTO.class);
                        if (personalInfo != null) {
                            dto.setName(personalInfo.getName());
                            dto.setRegion(personalInfo.getRegion());
                        }
                    } else if ("company".equals(kyc.getType()) && kyc.getCompanyInfo() != null) {
                        CompanyInfoDTO companyInfo = objectMapper.readValue(
                                kyc.getCompanyInfo(), CompanyInfoDTO.class);
                        if (companyInfo != null) {
                            dto.setName(companyInfo.getCompanyName());
                            dto.setRegion(companyInfo.getRegion());
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing JSON for user {}: {}", user.getId(), e.getMessage());
                    // Continue processing other users instead of breaking
                    continue;
                }
            }

            dtoList.add(dto);
        }

        // Calculate total count
        Long total = agencyUserMapper.selectCount(queryWrapper);

        // Create result map
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (total + size - 1) / size);
        result.put("current", page);
        result.put("size", size);
        result.put("list", dtoList);

        return R.success(result);
    }

    @Transactional
    public R auditKyc(AgencyKycAuditRequest request) {
        AgencyKyc kyc = agencyKycMapper.selectById(request.getId());
        if (kyc == null) {
            return R.error("KYC application not found");
        }

        try {
            if (request.getApproved()) {
                // Handle approval
                kyc.setStatus("approved");
                kyc.setApprovedAt(LocalDateTime.now());

                // Update agreement info
                AgreementInfoDTO agreementInfo = new AgreementInfoDTO();
                agreementInfo.setAgreementUrl(request.getAgreementUrl());

                kyc.setAgreementInfo(objectMapper.writeValueAsString(agreementInfo));
            } else {
                // Handle rejection
                kyc.setStatus("rejected");
                kyc.setRejectReason(request.getRejectReason());
            }

            kyc.setUpdatedAt(LocalDateTime.now());
            agencyKycMapper.updateById(kyc);

            return R.success( "KYC audit processed successfully");
        } catch (Exception e) {
            return R.error("Failed to process KYC audit: " + e.getMessage());
        }
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_ATTEMPTS = 10;

    private String generateUniqueInviteCode(int length) {
        Random random = new Random();
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            StringBuilder code = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }

            LambdaQueryWrapper<AgencyUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgencyUser::getInviteCode, code.toString());

            if (agencyUserMapper.selectCount(queryWrapper) == 0) {
                return code.toString();
            }
            attempts++;
        }
        return null;
    }

    @Override
    public R createAgency(CreateAgencyRequest request) {
        if (agencyUserMapper.selectOne(new LambdaQueryWrapper<AgencyUser>()
                .eq(AgencyUser::getEmail, request.getEmail())) != null) {
            return R.error("邮箱已经存在");
        }

        String inviteCode = generateUniqueInviteCode(10);
        if (inviteCode == null) {
            return R.error(" 无法生成唯一邀请码");
        }

        AgencyUser agencyUser = new AgencyUser();
        agencyUser.setName(request.getName());
        agencyUser.setEmail(request.getEmail());
        agencyUser.setPhone(request.getPhone());
        agencyUser.setStatus(0);
        agencyUser.setKycStatus(0);
        agencyUser.setInviteCode(inviteCode);
        agencyUser.setCreateTime(LocalDateTime.now());
        agencyUser.setUpdateTime(LocalDateTime.now());

        agencyUserMapper.insert(agencyUser);

        Map<String, Object> result = new HashMap<>();
        result.put("email", agencyUser.getEmail());
        result.put("phone", agencyUser.getPhone());
        result.put("inviteCode", inviteCode);

        return R.success(result);
    }

    @Override
    public R sendAgencyEmail(AgencyEmailRequest request) {
        AgencyUser user = agencyUserMapper.selectOne(new LambdaQueryWrapper<AgencyUser>()
                .eq(AgencyUser::getEmail, request.getEmail()));

        if (user == null) {
            return R.error("用户不存在");
        }

        try {
            emailService.sendEmail(request.getEmail(), request.getSubject(), request.getContent());
            return R.success("邮件发送成功");
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return R.error("发送邮件失败：" + e.getMessage());
        }
    }
}