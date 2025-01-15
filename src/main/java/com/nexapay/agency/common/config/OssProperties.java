package com.nexapay.agency.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    @NotBlank(message = "OSS endpoint must not be empty")
    private String endpoint;

    @NotBlank(message = "OSS accessKeyId must not be empty")
    private String accessKeyId;

    @NotBlank(message = "OSS accessKeySecret must not be empty")
    private String accessKeySecret;

    @NotBlank(message = "OSS bucketName must not be empty")
    private String bucketName;

    private String urlPrefix;
}