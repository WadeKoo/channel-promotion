package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_leads")
public class MerchantLead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agencyId;
    private String inviteCode;
    private String salesName;
    private String email;
    private String phone;
    private String wechat;
    private Integer status; // 0: pending, 1: contacted, 2: converted, 3: rejected
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastSyncTime;
    private Integer syncStatus; // 0: not synced, 1: sync success, 2: sync failed
    private String syncFailReason;
}
