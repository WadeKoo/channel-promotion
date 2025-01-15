package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件并返回资源URL
     */
    R<String> uploadFile(MultipartFile file);
}
