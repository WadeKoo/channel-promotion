package com.nexapay.agency.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgencyKycAuditRequest {
    @NotNull(message = "KYC ID cannot be empty")
    private String id;

    @NotNull(message = "Audit action cannot be empty")
    private Boolean approved;

    private String rejectReason;

    private String agreementUrl;  // Contract URL for approval case
}
