package com.nexapay.agency.dto.agency;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantListVO {
    private String merchantName;
    private LocalDateTime firstContactTime;
    private BigDecimal totalTransactionAmount;
    private LocalDateTime lastTransactionTime;
    private String registerStatus;
    private String kycStatus;
}