package com.nexapay.promotion.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantVerificationListDTO {
    private String id;
    private String name;  // personal name or company name
    private String email;
    private String type;  // personal/company
    private LocalDateTime createdAt;
    private String status; // added status field
}