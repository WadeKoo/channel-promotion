package com.nexapay.agency.dto.admin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AgencyListDTO {
    private Long id;
    private String email;
    private String name;
    private String type;  // personal/company
    private String region;
    private BigDecimal commissionRate;
    private BigDecimal firstOrderBonus;
    private Integer kycStatus;
    private Integer status;
    private LocalDateTime createTime;
}
