package com.nexapay.agency.dto.agency;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PersonalInfoDTO {
    @NotNull(message = "国家不能为空")
    private String region;
    @NotNull(message = "姓名不能为空")
    private String name;
    @NotNull(message = "证件类型不能为空")
    private String idType;
    @NotNull(message = "证件号码不能为空")
    private String idNumber;
    private PhoneDTO phone;
    private String verificationCode;
}
