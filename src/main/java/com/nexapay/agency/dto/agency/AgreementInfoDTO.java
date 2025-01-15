package com.nexapay.agency.dto.agency;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgreementInfoDTO {
    @NotNull(message = "请确认同意协议")
    private Boolean agreed;

    @NotNull(message = "签名不能为空")
    private String signature;

    private LocalDateTime signedAt;
}
