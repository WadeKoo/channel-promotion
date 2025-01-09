package com.nexapay.promotion.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileInfoDTO {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
}
