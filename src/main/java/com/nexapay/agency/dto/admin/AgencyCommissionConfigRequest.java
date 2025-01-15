package com.nexapay.agency.dto.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgencyCommissionConfigRequest {
    @NotNull(message = "渠道商ID不能为空")
    private Long agencyUserId;

    @NotNull(message = "佣金比例不能为空")
    @DecimalMin(value = "0.00", message = "佣金比例不能小于0")
    @DecimalMax(value = "100.00", message = "佣金比例不能大于100")
    private BigDecimal commissionRate;

    @NotNull(message = "首单奖励金额不能为空")
    @DecimalMin(value = "0.00", message = "首单奖励金额不能小于0")
    private BigDecimal firstOrderBonus;
}