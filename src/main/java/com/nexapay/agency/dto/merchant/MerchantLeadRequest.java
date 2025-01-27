package com.nexapay.agency.dto.merchant;

import lombok.Data;

import java.time.LocalDateTime;

public class MerchantLeadRequest {
    @Data
    public static class Register {
        private String inviteCode;
        private String email;
        private String phone;
        private String wechat;
    }

    @Data
    public static class Update {
        private Long id;
        private String email;
        private String phone;
        private String wechat;
    }

    @Data
    public static class UpdateStatus {
        private Long id;
        private Integer status;
    }

    @Data
    public static class UpdateSales {
        private Long id;
        private String salesName;
    }

}

