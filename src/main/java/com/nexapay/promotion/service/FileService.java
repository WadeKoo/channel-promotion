package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件并返回资源URL
     */
    R<String> uploadFile(MultipartFile file);
}
