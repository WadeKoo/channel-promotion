package com.nexapay.agency.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgencyManagementDashboardVO {
    private Long totalAgencies;
    private Long totalPromotedMerchants;
    private Long registeredMerchants;
    private Long activeMerchants;
    private BigDecimal totalTransactionAmount;
    private BigDecimal totalCommission;
}