package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.*;
import com.nexapay.agency.entity.AgencyCommissionConfig;
import com.nexapay.agency.exception.BusinessException;
import com.nexapay.agency.entity.AgencyKyc;
import com.nexapay.agency.mapper.AgencyCommissionConfigMapper;
import com.nexapay.agency.mapper.AgencyKycMapper;
import com.nexapay.agency.mapper.AgencyUserMapper;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.service.AgencyKycService;
import com.nexapay.agency.service.FileService;
import com.nexapay.agency.constants.AgencyKycConstants;
import com.nexapay.agency.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyKycServiceImpl implements AgencyKycService {

    private final AgencyKycMapper userKycMapper;
    private final ObjectMapper objectMapper;
    private final FileService fileService;
    private final AgencyUserMapper agencyUserMapper;


    private final AgencyCommissionConfigMapper agencyCommissionConfigMapper;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int INVITE_CODE_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 10;

    @Override
    @Transactional
    public R<KycDTO> initKyc(String type) {
        // 验证类型
        if (!AgencyKycConstants.Type.PERSONAL.equals(type)
                && !AgencyKycConstants.Type.COMPANY.equals(type)) {
            return R.error("不支持的认证类型");
        }

        // 检查是否已存在进行中的认证
        Long currentUserId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<AgencyKyc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgencyKyc::getUserId, currentUserId)
                .ne(AgencyKyc::getStatus, AgencyKycConstants.Status.APPROVED);

        AgencyKyc existingKyc = userKycMapper.selectOne(queryWrapper);
        if (existingKyc != null) {
            return R.error("已存在进行中的认证申请");
        }

        // 创建新的认证记录
        AgencyKyc kyc = new AgencyKyc();
        kyc.setId(UUID.randomUUID().toString()); // 设置UUID作为ID
        kyc.setUserId(currentUserId);
        kyc.setType(type);
        kyc.setStatus(AgencyKycConstants.Status.DRAFT);
        kyc.setCreatedAt(LocalDateTime.now());
        kyc.setUpdatedAt(LocalDateTime.now());

        userKycMapper.insert(kyc);
        return R.success(convertToDTO(kyc));
    }

    @Override
    public R<KycDTO> getKyc() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<AgencyKyc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgencyKyc::getUserId, currentUserId);

        AgencyKyc kyc = userKycMapper.selectOne(queryWrapper);

        if (kyc == null) {
            kyc = new AgencyKyc();
        }

        return R.success(convertToDTO(kyc));
    }

    @Override
    @Transactional
    public R<KycDTO> updatePersonalInfo(String id, PersonalInfoDTO personalInfo) {
        AgencyKyc kyc = getKycForUpdate(id);
        if (kyc == null) {
            return R.error("认证记录不存在或无权操作");
        }

        if (!AgencyKycConstants.Type.PERSONAL.equals(kyc.getType())) {
            return R.error("非个人认证不能更新个人信息");
        }

        try {
            kyc.setPersonalInfo(objectMapper.writeValueAsString(personalInfo));

            // Update user name
            AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());
            if (user != null && personalInfo.getName() != null) {
                user.setName(personalInfo.getName());
                user.setUpdateTime(LocalDateTime.now());
                agencyUserMapper.updateById(user);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize personal info", e);
            return R.error("更新个人信息失败");
        }

        kyc.setUpdatedAt(LocalDateTime.now());
        userKycMapper.updateById(kyc);
        return R.success(convertToDTO(kyc));
    }

    @Override
    @Transactional
    public R<KycDTO> updateCompanyInfo(String id, CompanyInfoDTO companyInfo) {
        AgencyKyc kyc = getKycForUpdate(id);
        if (kyc == null) {
            return R.error("认证记录不存在或无权操作");
        }

        if (!AgencyKycConstants.Type.COMPANY.equals(kyc.getType())) {
            return R.error("非企业认证不能更新企业信息");
        }

        try {
            kyc.setCompanyInfo(objectMapper.writeValueAsString(companyInfo));


            AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());
            if (user != null && companyInfo.getCompanyName() != null) {
                user.setName(companyInfo.getCompanyName());
                user.setUpdateTime(LocalDateTime.now());
                agencyUserMapper.updateById(user);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize company info", e);
            return R.error("更新企业信息失败");
        }

        kyc.setUpdatedAt(LocalDateTime.now());
        userKycMapper.updateById(kyc);
        return R.success(convertToDTO(kyc));
    }

    @Override
    @Transactional
    public R<KycDTO> updateBankInfo(String id, BankInfoDTO bankInfo) {
        AgencyKyc kyc = getKycForUpdate(id);
        if (kyc == null) {
            return R.error("认证记录不存在或无权操作");
        }

        try {
            kyc.setBankInfo(objectMapper.writeValueAsString(bankInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize bank info", e);
            return R.error("更新银行账户信息失败");
        }

        kyc.setUpdatedAt(LocalDateTime.now());
        userKycMapper.updateById(kyc);
        return R.success(convertToDTO(kyc));
    }

    @Override
    @Transactional
    public R<KycDTO> updateDocumentAndSubmit(String id, DocumentsDTO documents) {
        AgencyKyc kyc = getKycForUpdate(id);
        if (kyc == null) {
            return R.error("认证记录不存在或无权操作");
        }

        try {
            // Update documents
            kyc.setDocuments(objectMapper.writeValueAsString(documents));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize documents", e);
            return R.error("更新文档信息失败");
        }


        // Validate and submit
        try {
            validateRequiredInfo(kyc);
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }

        kyc.setStatus(AgencyKycConstants.Status.SUBMITTED);
        kyc.setSubmittedAt(LocalDateTime.now());
        kyc.setUpdatedAt(LocalDateTime.now());

        userKycMapper.updateById(kyc);
        return R.success(convertToDTO(kyc));
    }

    @Override
    @Transactional
    public R updateAgreement(String id, AgreementInfoDTO agreementInfo) {
        AgencyKyc kyc = getKycForAgreement(id);
        if (kyc == null) {
            return R.error("认证记录不存在或无权操作");
        }

        agreementInfo.setSignedAt(LocalDateTime.now());
        try {
            kyc.setAgreementInfo(objectMapper.writeValueAsString(agreementInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize agreement info", e);
            return R.error("更新协议信息失败");
        }

        kyc.setStatus(AgencyKycConstants.Status.DONE);
        kyc.setUpdatedAt(LocalDateTime.now());
        userKycMapper.updateById(kyc);

        // Update user KYC status and generate invite code if needed
        AgencyUser user = agencyUserMapper.selectById(kyc.getUserId());
        if (user != null) {
            user.setKycStatus(1);

            // Generate invite code if empty
            if (user.getInviteCode() == null || user.getInviteCode().trim().isEmpty()) {
                String inviteCode = generateUniqueInviteCode();
                if (inviteCode == null) {
                    log.error("Failed to generate unique invite code after maximum attempts");
                    return R.error("系统错误，请稍后重试");
                }
                user.setInviteCode(inviteCode);
            }

            user.setUpdateTime(LocalDateTime.now());
            agencyUserMapper.updateById(user);

            Map<String, Object> result = new HashMap<>();

            result.put("userInfo", new HashMap<String, Object>() {{
                put("email", user.getEmail());
                put("kycStatus", user.getKycStatus());
                put("inviteCode", user.getInviteCode());
            }});

            return R.success(result);
        } else {
            return R.error("用户不存在");
        }
    }

    private String generateUniqueInviteCode() {
        Random random = new Random();
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }

            // Check if code already exists
            LambdaQueryWrapper<AgencyUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AgencyUser::getInviteCode, code.toString());

            if (agencyUserMapper.selectCount(queryWrapper) == 0) {
                return code.toString();
            }

            attempts++;
        }

        return null; // Return null if unable to generate unique code after max attempts
    }


    private AgencyKyc getKycById(String id) {
        return userKycMapper.selectById(id);
    }

    private AgencyKyc getKycForUpdate(String id) {
        AgencyKyc kyc = getKycById(id);
        if (kyc == null) {
            return null;
        }

        // 权限检查
        if (!kyc.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            return null;
        }

        // 状态检查
        if (!AgencyKycConstants.Status.DRAFT.equals(kyc.getStatus())) {
            return null;
        }

        return kyc;
    }

    private AgencyKyc getKycForAgreement(String id) {
        // 权限检查
        AgencyKyc kyc = getKycById(id);
        if (!kyc.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            return null;
        }

        // 状态检查
        if (!AgencyKycConstants.Status.APPROVED.equals(kyc.getStatus())) {
            return null;
        }

        return kyc;

    }

    private void validateRequiredInfo(AgencyKyc kyc) {
        try {
            // 验证基本信息
            if (AgencyKycConstants.Type.PERSONAL.equals(kyc.getType())) {
                if (kyc.getPersonalInfo() == null) {
                    throw new BusinessException("请完善个人信息");
                }
                PersonalInfoDTO personalInfo = objectMapper.readValue(
                        kyc.getPersonalInfo(),
                        PersonalInfoDTO.class
                );
                validatePersonalInfo(personalInfo);
            } else {
                if (kyc.getCompanyInfo() == null) {
                    throw new BusinessException("请完善企业信息");
                }
                CompanyInfoDTO companyInfo = objectMapper.readValue(
                        kyc.getCompanyInfo(),
                        CompanyInfoDTO.class
                );
                validateCompanyInfo(companyInfo);
            }

            // 验证银行信息
            if (kyc.getBankInfo() == null) {
                throw new BusinessException("请完善银行账户信息");
            }
            BankInfoDTO bankInfo = objectMapper.readValue(
                    kyc.getBankInfo(),
                    BankInfoDTO.class
            );
            validateBankInfo(bankInfo);

            // 验证文档信息
            if (kyc.getDocuments() == null) {
                throw new BusinessException("请上传所需文件");
            }
            DocumentsDTO documents = objectMapper.readValue(
                    kyc.getDocuments(),
                    DocumentsDTO.class
            );
            validateDocuments(documents, kyc.getType());


        } catch (JsonProcessingException e) {
            log.error("Failed to validate kyc info", e);
            throw new BusinessException("验证信息格式错误");
        }
    }

    private void validatePersonalInfo(PersonalInfoDTO personalInfo) {
        if (personalInfo.getRegion() == null || personalInfo.getRegion().trim().isEmpty()) {
            throw new BusinessException("请选择国家/地区");
        }
        if (personalInfo.getName() == null || personalInfo.getName().trim().isEmpty()) {
            throw new BusinessException("请输入姓名");
        }

        if (personalInfo.getIdNumber() == null || personalInfo.getIdNumber().trim().isEmpty()) {
            throw new BusinessException("请输入证件号码");
        }
        if (personalInfo.getPhone() == null) {
            throw new BusinessException("请输入联系电话");
        }
    }

    private void validateCompanyInfo(CompanyInfoDTO companyInfo) {

        if (companyInfo.getCompanyName() == null || companyInfo.getCompanyName().trim().isEmpty()) {
            throw new BusinessException("请输入公司名称");
        }
        if (companyInfo.getContactName() == null || companyInfo.getContactName().trim().isEmpty()) {
            throw new BusinessException("请输入联系人姓名");
        }
        if (companyInfo.getContactPhone() == null) {
            throw new BusinessException("请输入联系人电话");
        }
        if (companyInfo.getEmail() == null || companyInfo.getEmail().trim().isEmpty()) {
            throw new BusinessException("请输入联系邮箱");
        }
    }

    private void validateBankInfo(BankInfoDTO bankInfo) {

        if (bankInfo.getBankName() == null || bankInfo.getBankName().trim().isEmpty()) {
            throw new BusinessException("请输入银行名称");
        }
        if (bankInfo.getAccountHolder() == null || bankInfo.getAccountHolder().trim().isEmpty()) {
            throw new BusinessException("请输入账户持有人姓名");
        }
        if (bankInfo.getAccountNumber() == null || bankInfo.getAccountNumber().trim().isEmpty()) {
            throw new BusinessException("请输入账号");
        }
        if (bankInfo.getCurrency() == null || bankInfo.getCurrency().trim().isEmpty()) {
            throw new BusinessException("请选择币种");
        }
    }

    private void validateDocuments(DocumentsDTO documents, String type) {
        if (documents.getIdFront() == null) {
            throw new BusinessException("请上传证件正面照片");
        }
        if (documents.getIdBack() == null) {
            throw new BusinessException("请上传证件背面照片");
        }
        if (documents.getBankStatement() == null) {
            throw new BusinessException("请上传银行对账单");
        }

    }

    private void validateAgreementInfo(AgreementInfoDTO agreementInfo) {
        if (agreementInfo.getAgreed() == null || !agreementInfo.getAgreed()) {
            throw new BusinessException("请同意服务协议");
        }
        if (agreementInfo.getSignature() == null || agreementInfo.getSignature().trim().isEmpty()) {
            throw new BusinessException("请签署协议");
        }
        if (agreementInfo.getSignedAt() == null) {
            throw new BusinessException("签署时间异常");
        }
    }

    private KycDTO convertToDTO(AgencyKyc kyc) {
        KycDTO dto = new KycDTO();
        dto.setId(kyc.getId());
        dto.setType(kyc.getType());
        dto.setStatus(kyc.getStatus());
        dto.setRejectReason(kyc.getRejectReason());
        dto.setSubmittedAt(kyc.getSubmittedAt());
        dto.setApprovedAt(kyc.getApprovedAt());

        try {
            // Convert personal info
            if (kyc.getPersonalInfo() != null) {
                dto.setPersonalInfo(objectMapper.readValue(
                        kyc.getPersonalInfo(),
                        PersonalInfoDTO.class
                ));
            }

            // Convert company info
            if (kyc.getCompanyInfo() != null) {
                dto.setCompanyInfo(objectMapper.readValue(
                        kyc.getCompanyInfo(),
                        CompanyInfoDTO.class
                ));
            }

            // Convert bank info
            if (kyc.getBankInfo() != null) {
                dto.setBankInfo(objectMapper.readValue(
                        kyc.getBankInfo(),
                        BankInfoDTO.class
                ));
            }

            // Convert documents
            if (kyc.getDocuments() != null) {
                dto.setDocuments(objectMapper.readValue(
                        kyc.getDocuments(),
                        DocumentsDTO.class
                ));
            }

            // Convert agreement info
            if (kyc.getAgreementInfo() != null) {
                dto.setAgreementInfo(objectMapper.readValue(
                        kyc.getAgreementInfo(),
                        AgreementInfoDTO.class
                ));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize kyc info", e);
            throw new BusinessException("转换认证信息失败");
        }

        return dto;
    }

    @Override
    public R<AgencyInfoDTO> getAgencyInfo() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        AgencyInfoDTO info = new AgencyInfoDTO();

        // Get user info
        AgencyUser user = agencyUserMapper.selectById(currentUserId);


        user.setPassword(null);
        info.setUser(user);


        // Get KYC info
        AgencyKyc kyc = userKycMapper.selectOne(
                new LambdaQueryWrapper<AgencyKyc>()
                        .eq(AgencyKyc::getUserId, currentUserId)
                        .orderByDesc(AgencyKyc::getCreatedAt)
                        .last("LIMIT 1")
        );
        info.setKyc(convertToKycDTO(kyc));

        // Get commission config
        AgencyCommissionConfig commission = agencyCommissionConfigMapper.selectOne(
                new LambdaQueryWrapper<AgencyCommissionConfig>()
                        .eq(AgencyCommissionConfig::getAgencyUserId, currentUserId)
        );
        info.setCommission(commission);

        return R.success(info);
    }

    private KycDTO convertToKycDTO(AgencyKyc kyc) {
        if (kyc == null) {
            return null;
        }
        KycDTO dto = new KycDTO();
        BeanUtils.copyProperties(kyc, dto);

        try {
            // Convert personal info
            if (kyc.getPersonalInfo() != null) {
                dto.setPersonalInfo(objectMapper.readValue(
                        kyc.getPersonalInfo(),
                        PersonalInfoDTO.class
                ));
            }

            // Convert company info
            if (kyc.getCompanyInfo() != null) {
                dto.setCompanyInfo(objectMapper.readValue(
                        kyc.getCompanyInfo(),
                        CompanyInfoDTO.class
                ));
            }

            // Convert bank info
            if (kyc.getBankInfo() != null) {
                dto.setBankInfo(objectMapper.readValue(
                        kyc.getBankInfo(),
                        BankInfoDTO.class
                ));
            }

            // Convert documents
            if (kyc.getDocuments() != null) {
                dto.setDocuments(objectMapper.readValue(
                        kyc.getDocuments(),
                        DocumentsDTO.class
                ));
            }

            // Convert agreement info
            if (kyc.getAgreementInfo() != null) {
                dto.setAgreementInfo(objectMapper.readValue(
                        kyc.getAgreementInfo(),
                        AgreementInfoDTO.class
                ));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize kyc info", e);
            throw new BusinessException("转换认证信息失败");
        }

        return dto;
    }

}