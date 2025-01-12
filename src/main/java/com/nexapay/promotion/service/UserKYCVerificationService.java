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
    R<VerificationDTO> getVerification();

    /**
     * 更新个人信息
     */
    R<VerificationDTO> updatePersonalInfo(String id, PersonalInfoDTO personalInfo);

    /**
     * 更新企业信息
     */
    R<VerificationDTO> updateCompanyInfo(String id, CompanyInfoDTO companyInfo);

    /**
     * 更新银行账户信息
     */
    R<VerificationDTO> updateBankInfo(String id, BankInfoDTO bankInfo);

    /**
     * 更新文件信息并提交认证申请
     */
    R<VerificationDTO> updateDocumentAndSubmit(String id, DocumentsDTO documents);

    /**
     * 更新协议签署信息
     */
    R<VerificationDTO> updateAgreement(String id, AgreementInfoDTO agreementInfo);
}