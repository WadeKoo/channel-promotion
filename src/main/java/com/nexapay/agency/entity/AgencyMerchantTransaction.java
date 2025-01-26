package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("agency_merchant_transactions")
public class AgencyMerchantTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agencyId;
    private String merchantId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private Integer status;
    private LocalDateTime transactionTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}