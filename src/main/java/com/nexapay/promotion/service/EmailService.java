package com.nexapay.promotion.service;

public interface EmailService {
    /**
     * 发送验证码邮件
     * @param to 收件人
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送通用邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendEmail(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
}