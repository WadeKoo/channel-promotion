package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.*;

public interface AgencyKycService {
    /**
     * 初始化认证申请
     */
    R<KycDTO> initKyc(String type);

    /**
     * 获取认证申请详情
     */
    R<KycDTO> getKyc();

    /**
     * 更新个人信息
     */
    R<KycDTO> updatePersonalInfo(String id, PersonalInfoDTO personalInfo);

    /**
     * 更新企业信息
     */
    R<KycDTO> updateCompanyInfo(String id, CompanyInfoDTO companyInfo);

    /**
     * 更新银行账户信息
     */
    R<KycDTO> updateBankInfo(String id, BankInfoDTO bankInfo);

    /**
     * 更新文件信息并提交认证申请
     */
    R<KycDTO> updateDocumentAndSubmit(String id, DocumentsDTO documents);

    /**
     * 更新协议签署信息
     */
    R updateAgreement(String id, AgreementInfoDTO agreementInfo);
}