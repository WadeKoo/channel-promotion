package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface UserKYCVerificationService {
    /**
     * 初始化认证申请
     */
    R<VerificationDTO> initVerification(String type);

    /**
     * 获取认证申请详情
     */
    R<VerificationDTO> getVerification(Long id);

    /**
     * 更新个人信息
     */
    R<VerificationDTO> updatePersonalInfo(Long id, PersonalInfoDTO personalInfo);

    /**
     * 更新企业信息
     */
    R<VerificationDTO> updateCompanyInfo(Long id, CompanyInfoDTO companyInfo);

    /**
     * 更新银行账户信息
     */
    R<VerificationDTO> updateBankInfo(Long id, BankInfoDTO bankInfo);

    /**
     * 上传认证文件
     */
    R<VerificationDTO> uploadDocument(Long id, MultipartFile file, String documentType);

    /**
     * 更新协议签署信息
     */
    R<VerificationDTO> updateAgreement(Long id, AgreementInfoDTO agreementInfo);

    /**
     * 提交认证申请
     */
    R<VerificationDTO> submit(Long id);
}