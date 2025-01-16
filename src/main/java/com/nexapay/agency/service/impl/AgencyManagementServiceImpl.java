package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AgencyCommissionConfigRequest;
import com.nexapay.agency.dto.admin.AgencyKycAuditRequest;
import com.nexapay.agency.dto.admin.AgencyListDTO;
import com.nexapay.agency.dto.agency.AgreementInfoDTO;
import com.nexapay.agency.dto.agency.CompanyInfoDTO;
import com.nexapay.agency.dto.agency.PersonalInfoDTO;
import com.nexapay.agency.dto.admin.AgencyKycListDTO;
import com.nexapay.agency.entity.AgencyCommissionConfig;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.entity.AgencyKyc;
import com.nexapay.agency.mapper.AgencyCommissionConfigMapper;
import com.nexapay.agency.mapper.AgencyKycMapper;
import com.nexapay.agency.mapper.AgencyUserMapper;
import com.nexapay.agency.service.AgencyManagementService;
import com.nexapay.agency.util.SecurityUtils;
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
public class AgencyManagementServiceImpl implements AgencyManagementService {

    private final AgencyKycMapper agencyKycMapper;
    private final AgencyCommissionConfigMapper agencyCommissionConfigMapper;
    private final AgencyUserMapper agencyUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    public R getKycList(Integer page, Integer size) {
        // Create pagination object
        Page<AgencyKyc> pageParam = new Page<>(page, size);

        // Query conditions - put submitted status first, then order by creation date
        LambdaQueryWrapper<AgencyKyc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .last("ORDER BY FIELD(status, 'submitted', 'approved', 'rejected') ASC, created_at DESC");

        // Execute query
        Page<AgencyKyc> kycPage = agencyKycMapper.selectPage(pageParam, queryWrapper);

        // Transform results
        List<AgencyKycListDTO> dtoList = new ArrayList<>();
        for (AgencyKyc kyc : kycPage.getRecords()) {
            AgencyKycListDTO dto = new AgencyKycListDTO();
            dto.setId(kyc.getId());
            dto.setType(kyc.getType());
            dto.setCreatedAt(kyc.getCreatedAt());
            dto.setStatus(kyc.getStatus());

            // Get user email
            AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());
            if (user != null) {
                dto.setEmail(user.getEmail());
            }

            // Get name based on type
            try {
                if ("personal".equals(kyc.getType())) {
                    PersonalInfoDTO personalInfo = objectMapper.readValue(
                            kyc.getPersonalInfo(), PersonalInfoDTO.class);
                    dto.setName(personalInfo.getName());
                } else {
                    CompanyInfoDTO companyInfo = objectMapper.readValue(
                            kyc.getCompanyInfo(), CompanyInfoDTO.class);
                    dto.setName(companyInfo.getCompanyName());
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON for kyc {}: {}",
                        kyc.getId(), e.getMessage());
                continue;
            }

            dtoList.add(dto);
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
        // 参数校验
        page = (page == null || page < 1) ? 1 : page;
        size = (size == null || size < 1) ? 10 : size;

        // 创建分页对象
        Page<AgencyUser> pageParam = new Page<>(page, size);

        // 查询条件
        LambdaQueryWrapper<AgencyUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AgencyUser::getCreateTime);

        // 执行查询
        Page<AgencyUser> userPage = agencyUserMapper.selectPage(pageParam, queryWrapper);

        // 转换结果
        List<AgencyListDTO> dtoList = new ArrayList<>();
        for (AgencyUser user : userPage.getRecords()) {
            AgencyListDTO dto = new AgencyListDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setStatus(user.getStatus());
            dto.setKycStatus(user.getKycStatus());
            dto.setCreateTime(user.getCreateTime());

            // 获取佣金配置
            LambdaQueryWrapper<AgencyCommissionConfig> configWrapper = new LambdaQueryWrapper<>();
            configWrapper.eq(AgencyCommissionConfig::getAgencyUserId, user.getId())
                    .eq(AgencyCommissionConfig::getStatus, 1);
            AgencyCommissionConfig config = agencyCommissionConfigMapper.selectOne(configWrapper);

            if (config != null) {
                dto.setCommissionRate(config.getCommissionRate());
                dto.setFirstOrderBonus(config.getFirstOrderBonus());
            }

            // 获取KYC信息
            LambdaQueryWrapper<AgencyKyc> kycWrapper = new LambdaQueryWrapper<>();
            kycWrapper.eq(AgencyKyc::getUserId, user.getId())
                    .orderByDesc(AgencyKyc::getCreatedAt)
                    .last("LIMIT 1");
            AgencyKyc kyc = agencyKycMapper.selectOne(kycWrapper);

            if (kyc != null) {
                dto.setType(kyc.getType());
                try {
                    if ("personal".equals(kyc.getType())) {
                        PersonalInfoDTO personalInfo = objectMapper.readValue(
                                kyc.getPersonalInfo(), PersonalInfoDTO.class);
                        dto.setName(personalInfo.getName());
                        dto.setRegion(personalInfo.getCountry());
                    } else {
                        CompanyInfoDTO companyInfo = objectMapper.readValue(
                                kyc.getCompanyInfo(), CompanyInfoDTO.class);
                        dto.setName(companyInfo.getCompanyName());
                        dto.setRegion(companyInfo.getCountry());
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing JSON for user {}: {}", user.getId(), e.getMessage());
                }
            }

            dtoList.add(dto);
        }

        // 手动计算总记录数
        Long total = agencyUserMapper.selectCount(queryWrapper);

        // 创建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);  // 使用手动查询的总数
        result.put("pages", (total + size - 1) / size);  // 根据总数计算页数
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
}