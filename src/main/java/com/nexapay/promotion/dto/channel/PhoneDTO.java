package com.nexapay.promotion.dto.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhoneDTO {
    @NotNull(message = "国家代码不能为空")
    private String countryCode;

    @NotNull(message = "电话号码不能为空")
    private String number;
}
