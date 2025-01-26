package com.nexapay.agency.dto.agency;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AgencyMerchantDTO {
    private Long id;
    private String merchantId;
    private LocalDateTime firstContactTime;
    private Integer status;
    private BigDecimal totalTransactionAmount;
    private BigDecimal lastMonthTransactionAmount;
    private LocalDateTime lastTransactionTime;
    private String email;
    private String phone;
    private String wechat;
}
