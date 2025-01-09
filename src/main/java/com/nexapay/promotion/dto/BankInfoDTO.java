package com.nexapay.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class BankInfoDTO {
    @NotNull(message = "账户类型不能为空")
    private String accountType;
    @NotNull(message = "银行名称不能为空")
    private String bankName;
    private String swiftCode;
    @NotNull(message = "账户持有人不能为空")
    private String accountHolder;
    @NotNull(message = "账号不能为空")
    private String accountNumber;
    private String ibanNumber;
    @NotNull(message = "币种不能为空")
    private String currency;
    private String bankAddress;
    private String bankAddressOptional;
}
