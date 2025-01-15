package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("agency_commission_config")
public class AgencyCommissionConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agencyUserId;
    private BigDecimal commissionRate;
    private BigDecimal firstOrderBonus;
    private Integer status;
    private Long createBy;
    private Long updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
