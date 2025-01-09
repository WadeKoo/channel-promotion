package com.nexapay.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.promotion.common.R;
import com.nexapay.promotion.exception.BusinessException;
import com.nexapay.promotion.dto.*;
import com.nexapay.promotion.entity.UserKYCVerification;
import com.nexapay.promotion.mapper.UserKYCVerificationMapper;
import com.nexapay.promotion.service.UserKYCVerificationService;
import com.nexapay.promotion.service.FileService;
import com.nexapay.promotion.constants.UserKYCVerificationConstants;
import com.nexapay.promotion.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKYCVerificationServiceImpl implements UserKYCVerificationService {

    private final UserKYCVerificationMapper userKYCVerificationMapper;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    @Override
    @Transactional
    public R<VerificationDTO> initVerification(String type) {
        // 验证类型
        if (!UserKYCVerificationConstants.Type.PERSONAL.equals(type)
                && !UserKYCVerificationConstants.Type.COMPANY.equals(type)) {
            return R.error("不支持的认证类型");
        }

        // 检查是否已存在进行中的认证
        Long currentUserId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<UserKYCVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserKYCVerification::getUserId, currentUserId)
                .ne(UserKYCVerification::getStatus, UserKYCVerificationConstants.Status.APPROVED);

        UserKYCVerification existingVerification = userKYCVerificationMapper.selectOne(queryWrapper);
        if (existingVerification != null) {
            return R.error("已存在进行中的认证申请");
        }

        // 创建新的认证记录
        UserKYCVerification verification = new UserKYCVerification();
        verification.setUserId(currentUserId);
        verification.setType(type);
        verification.setStatus(UserKYCVerificationConstants.Status.DRAFT);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());

        userKYCVerificationMapper.insert(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    public R<VerificationDTO> getVerification(Long id) {
        UserKYCVerification verification = getVerificationById(id);
        if (verification == null) {
            return R.error("认证记录不存在");
        }
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> updatePersonalInfo(Long id, PersonalInfoDTO personalInfo) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        // 验证类型
        if (!UserKYCVerificationConstants.Type.PERSONAL.equals(verification.getType())) {
            return R.error("非个人认证不能更新个人信息");
        }

        // 设置个人信息
        try {
            verification.setPersonalInfo(objectMapper.writeValueAsString(personalInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize personal info", e);
            return R.error("更新个人信息失败");
        }

        verification.setUpdatedAt(LocalDateTime.now());
        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> updateCompanyInfo(Long id, CompanyInfoDTO companyInfo) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        if (!UserKYCVerificationConstants.Type.COMPANY.equals(verification.getType())) {
            return R.error("非企业认证不能更新企业信息");
        }

        try {
            verification.setCompanyInfo(objectMapper.writeValueAsString(companyInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize company info", e);
            return R.error("更新企业信息失败");
        }

        verification.setUpdatedAt(LocalDateTime.now());
        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> updateBankInfo(Long id, BankInfoDTO bankInfo) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        try {
            verification.setBankInfo(objectMapper.writeValueAsString(bankInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize bank info", e);
            return R.error("更新银行账户信息失败");
        }

        verification.setUpdatedAt(LocalDateTime.now());
        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> uploadDocument(Long id, MultipartFile file, String documentType) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        // 获取当前文档信息
        DocumentsDTO currentDocuments = null;
        try {
            currentDocuments = verification.getDocuments() != null ?
                    objectMapper.readValue(verification.getDocuments(), DocumentsDTO.class) :
                    new DocumentsDTO();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize documents", e);
            return R.error("处理文档信息失败");
        }

        // 上传文件
        FileInfoDTO fileInfo = fileService.uploadFile(file);

        // 根据文档类型更新对应字段
        switch (documentType) {
            case "idFront":
                currentDocuments.setIdFront(fileInfo);
                break;
            case "idBack":
                currentDocuments.setIdBack(fileInfo);
                break;
            case "bankStatement":
                currentDocuments.setBankStatement(fileInfo);
                break;
            case "businessLicense":
                if (!UserKYCVerificationConstants.Type.COMPANY.equals(verification.getType())) {
                    return R.error("非企业认证不能上传营业执照");
                }
                currentDocuments.setBusinessLicense(fileInfo);
                break;
            default:
                return R.error("不支持的文档类型");
        }

        try {
            verification.setDocuments(objectMapper.writeValueAsString(currentDocuments));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize documents", e);
            return R.error("更新文档信息失败");
        }

        verification.setUpdatedAt(LocalDateTime.now());
        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> updateAgreement(Long id, AgreementInfoDTO agreementInfo) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        agreementInfo.setSignedAt(LocalDateTime.now());
        try {
            verification.setAgreementInfo(objectMapper.writeValueAsString(agreementInfo));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize agreement info", e);
            return R.error("更新协议信息失败");
        }

        verification.setUpdatedAt(LocalDateTime.now());
        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    @Override
    @Transactional
    public R<VerificationDTO> submit(Long id) {
        UserKYCVerification verification = getVerificationForUpdate(id);
        if (verification == null) {
            return R.error("认证记录不存在或无权操作");
        }

        // 验证必填信息
        try {
            validateRequiredInfo(verification);
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }

        verification.setStatus(UserKYCVerificationConstants.Status.SUBMITTED);
        verification.setSubmittedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());

        userKYCVerificationMapper.updateById(verification);
        return R.success(convertToDTO(verification));
    }

    private UserKYCVerification getVerificationById(Long id) {
        return userKYCVerificationMapper.selectById(id);
    }

    private UserKYCVerification getVerificationForUpdate(Long id) {
        UserKYCVerification verification = getVerificationById(id);
        if (verification == null) {
            return null;
        }

        // 权限检查
        if (!verification.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            return null;
        }

        // 状态检查
        if (!UserKYCVerificationConstants.Status.DRAFT.equals(verification.getStatus())) {
            return null;
        }

        return verification;
    }

    private void validateRequiredInfo(UserKYCVerification verification) {
        try {
            // 验证基本信息
            if (UserKYCVerificationConstants.Type.PERSONAL.equals(verification.getType())) {
                if (verification.getPersonalInfo() == null) {
                    throw new BusinessException("请完善个人信息");
                }
                PersonalInfoDTO personalInfo = objectMapper.readValue(
                        verification.getPersonalInfo(),
                        PersonalInfoDTO.class
                );
                validatePersonalInfo(personalInfo);
            } else {
                if (verification.getCompanyInfo() == null) {
                    throw new BusinessException("请完善企业信息");
                }
                CompanyInfoDTO companyInfo = objectMapper.readValue(
                        verification.getCompanyInfo(),
                        CompanyInfoDTO.class
                );
                validateCompanyInfo(companyInfo);
            }

            // 验证银行信息
            if (verification.getBankInfo() == null) {
                throw new BusinessException("请完善银行账户信息");
            }
            BankInfoDTO bankInfo = objectMapper.readValue(
                    verification.getBankInfo(),
                    BankInfoDTO.class
            );
            validateBankInfo(bankInfo);

            // 验证文档信息
            if (verification.getDocuments() == null) {
                throw new BusinessException("请上传所需文件");
            }
            DocumentsDTO documents = objectMapper.readValue(
                    verification.getDocuments(),
                    DocumentsDTO.class
            );
            validateDocuments(documents, verification.getType());

            // 验证协议信息
            if (verification.getAgreementInfo() == null) {
                throw new BusinessException("请签署协议");
            }
            AgreementInfoDTO agreementInfo = objectMapper.readValue(
                    verification.getAgreementInfo(),
                    AgreementInfoDTO.class
            );
            validateAgreementInfo(agreementInfo);

        } catch (JsonProcessingException e) {
            log.error("Failed to validate verification info", e);
            throw new BusinessException("验证信息格式错误");
        }
    }

    private void validatePersonalInfo(PersonalInfoDTO personalInfo) {
        if (personalInfo.getCountry() == null || personalInfo.getCountry().trim().isEmpty()) {
            throw new BusinessException("请选择国家/地区");
        }
        if (personalInfo.getName() == null || personalInfo.getName().trim().isEmpty()) {
            throw new BusinessException("请输入姓名");
        }
        if (personalInfo.getIdType() == null || personalInfo.getIdType().trim().isEmpty()) {
            throw new BusinessException("请选择证件类型");
        }
        if (personalInfo.getIdNumber() == null || personalInfo.getIdNumber().trim().isEmpty()) {
            throw new BusinessException("请输入证件号码");
        }
        if (personalInfo.getPhone() == null) {
            throw new BusinessException("请输入联系电话");
        }
    }

    private void validateCompanyInfo(CompanyInfoDTO companyInfo) {
        if (companyInfo.getCountry() == null || companyInfo.getCountry().trim().isEmpty()) {
            throw new BusinessException("请选择公司注册地");
        }
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
        if (bankInfo.getAccountType() == null || bankInfo.getAccountType().trim().isEmpty()) {
            throw new BusinessException("请选择账户类型");
        }
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
        if (UserKYCVerificationConstants.Type.COMPANY.equals(type) && documents.getBusinessLicense() == null) {
            throw new BusinessException("请上传营业执照");
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

    private VerificationDTO convertToDTO(UserKYCVerification verification) {
        VerificationDTO dto = new VerificationDTO();
        dto.setId(verification.getId());
        dto.setType(verification.getType());
        dto.setStatus(verification.getStatus());
        dto.setRejectReason(verification.getRejectReason());
        dto.setSubmittedAt(verification.getSubmittedAt());
        dto.setApprovedAt(verification.getApprovedAt());

        try {
            // Convert personal info
            if (verification.getPersonalInfo() != null) {
                dto.setPersonalInfo(objectMapper.readValue(
                        verification.getPersonalInfo(),
                        PersonalInfoDTO.class
                ));
            }

            // Convert company info
            if (verification.getCompanyInfo() != null) {
                dto.setCompanyInfo(objectMapper.readValue(
                        verification.getCompanyInfo(),
                        CompanyInfoDTO.class
                ));
            }

            // Convert bank info
            if (verification.getBankInfo() != null) {
                dto.setBankInfo(objectMapper.readValue(
                        verification.getBankInfo(),
                        BankInfoDTO.class
                ));
            }

            // Convert documents
            if (verification.getDocuments() != null) {
                dto.setDocuments(objectMapper.readValue(
                        verification.getDocuments(),
                        DocumentsDTO.class
                ));
            }

            // Convert agreement info
            if (verification.getAgreementInfo() != null) {
                dto.setAgreementInfo(objectMapper.readValue(
                        verification.getAgreementInfo(),
                        AgreementInfoDTO.class
                ));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize verification info", e);
            throw new BusinessException("转换认证信息失败");
        }

        return dto;
    }
}