package com.nexapay.promotion.dto.channel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationDTO {
    private String id;
    private String type;
    private String status;
    private PersonalInfoDTO personalInfo;
    private CompanyInfoDTO companyInfo;
    private BankInfoDTO bankInfo;
    private DocumentsDTO documents;
    private AgreementInfoDTO agreementInfo;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}

