package com.nexapay.agency.dto.agency;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompanyInfoDTO {
    @NotNull(message = "国家/地区不能为空")
    private String region;
    @NotNull(message = "公司名称不能为空")
    private String companyName;
    @NotNull(message = "联系人姓名不能为空")
    private String contactName;
    private String contactPhone;
    @NotNull(message = "邮箱不能为空")
    private String email;
    private String businessType;
    private BigDecimal expectedVolume;
    private String license;

}