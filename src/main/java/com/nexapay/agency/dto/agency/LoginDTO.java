package com.nexapay.agency.dto.agency;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data

public class LoginDTO {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "平台类型不能为空")
    @Pattern(regexp = "^(agency|agency-admin)$", message = "平台类型不正确")
    private String platform;
}