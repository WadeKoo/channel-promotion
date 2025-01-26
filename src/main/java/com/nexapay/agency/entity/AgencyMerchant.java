package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agency_merchants")
public class AgencyMerchant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agencyId;
    private String merchantId;
    private String merchantName;
    private String email;
    private Integer registerStatus;
    private String kycStatus;
    private LocalDateTime merchantCreateTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastTransSyncTime;
}
