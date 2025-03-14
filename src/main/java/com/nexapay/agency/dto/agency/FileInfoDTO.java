package com.nexapay.agency.dto.agency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
}