package com.nexapay.agency.dto.admin;

import lombok.Data;

@Data
public class MerchantApiResponse {
    private String code;
    private String msg;
    private MerchantData data;
    private Boolean succ;

    @Data
    public static class MerchantData {
        private String merchantId;
        private String merchantName;
        private String email;
        private Integer registerStatus;
        private String kycStatus;
        private String createTime;
    }
}