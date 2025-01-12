package com.nexapay.promotion.dto;

import jakarta.validation.Valid;  // 使用 jakarta.validation
import lombok.Data;

public class KycVerificationRequest {
    @Data
    public static class Init {
        private String type;
    }

    @Data
    public static class UpdatePersonal {
        private String id;
        private PersonalInfoDTO personalInfo;
    }

    @Data
    public static class UpdateCompany {
        private String id;
        private CompanyInfoDTO companyInfo;
    }

    @Data
    public static class UpdateBank {
        private String id;
        private BankInfoDTO bankInfo;
    }

    @Data
    public static class UpdateDocument {
        private String id;
        private DocumentsDTO documents;
    }

    @Data
    public static class UpdateAgreement {
        private String id;
        private AgreementInfoDTO agreementInfo;
    }
}