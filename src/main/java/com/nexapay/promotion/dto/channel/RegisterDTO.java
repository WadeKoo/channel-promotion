package com.nexapay.promotion.dto.channel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;

    @NotBlank(message = "平台类型不能为空")
    @Pattern(regexp = "^(channel|channel-admin)$", message = "平台类型不正确")
    private String platform;
}
