package com.nexapay.agency.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgencyEmailRequest {
    @NotBlank(message = "邮箱 不能为空") //NotBlank(message = "邮箱不能为空）
    @Email
    private String email;

    @NotBlank(message = "邮件标题不能为空")
    private String subject;

    @NotBlank( message = "邮件内容不能为空")
    private String content;
}