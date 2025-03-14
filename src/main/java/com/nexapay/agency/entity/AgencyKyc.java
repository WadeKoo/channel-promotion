package com.nexapay.agency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agency_kyc")
public class AgencyKyc {
    @TableId(type = IdType.INPUT)
    private String id;
    private Long userId;
    private String type;  // personal/company
    private String status;  // draft/submitted/approved/rejected
    private String personalInfo;  // JSON string
    private String companyInfo;  // JSON string
    private String bankInfo;  // JSON string
    private String documents;  // JSON string
    private String agreementInfo;  // JSON string
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}
