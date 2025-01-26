package com.nexapay.agency.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class TransactionApiResponse {
    private String code;
    private String msg;
    private TransactionData data;
    private Boolean succ;

    @Data
    public static class TransactionData {
        private List<Transaction> list;
        private String total;
    }

    @Data
    public static class Transaction {
        private String transactionId;
        private String amount;
        private String currency;
        private Integer status;
        private String transactionTime;
    }
}
