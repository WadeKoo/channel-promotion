package com.nexapay.agency.dto.merchant;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantLeadDTO {
    private Long id;
    private Long agencyId;
    private String inviteCode;
    private String email;
    private String phone;
    private String wechat;
    private Integer status;
    private String agencyName;
    private String salesName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
