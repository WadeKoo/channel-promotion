package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("agency_first_order_bonus_record")
public class AgencyFirstOrderBonusRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agencyUserId;
    private Long merchantId;
    private String orderNo;
    private BigDecimal bonusAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}