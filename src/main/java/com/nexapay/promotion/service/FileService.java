package com.nexapay.promotion.service;

import com.nexapay.promotion.dto.FileInfoDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     */
    FileInfoDTO uploadFile(MultipartFile file);
}
