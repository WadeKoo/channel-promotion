package com.nexapay.agency.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgencyKycListDTO {
    private String id;
    private String name;  // personal name or company name
    private String email;
    private String type;  // personal/company
    private LocalDateTime createdAt;
    private String status; // added status field
}