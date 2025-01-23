package com.nexapay.agency.dto.agency;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BankInfoDTO {
    @NotNull(message = "国家/地区不能为空")
    private String region;

    @NotNull(message = "银行名称不能为空")
    private String bankName;

    @NotNull(message = "分行代码不能为空")
    private String branchCode;

    @NotNull(message = "账户持有人不能为空")
    private String accountHolder;

    @NotNull(message = "账号不能为空")
    private String accountNumber;

    private String swiftCode;

    private String ibanNumber;

    @NotNull(message = "币种不能为空")
    private String currency;

    @NotNull(message = "银行地址不能为空")
    private String bankAddress;
}