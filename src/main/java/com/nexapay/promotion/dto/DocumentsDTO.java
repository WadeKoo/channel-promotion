package com.nexapay.promotion.dto;

import lombok.Data;

@Data
public class DocumentsDTO {
    private FileInfoDTO idFront;
    private FileInfoDTO idBack;
    private FileInfoDTO bankStatement;
    private FileInfoDTO businessLicense;  // 用于企业认证
}