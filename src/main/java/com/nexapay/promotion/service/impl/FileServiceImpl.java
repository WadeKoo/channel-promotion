package com.nexapay.promotion.service.impl;

import com.nexapay.promotion.dto.FileInfoDTO;
import com.nexapay.promotion.exception.BusinessException;
import com.nexapay.promotion.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public FileInfoDTO uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 这里只是示例实现，实际项目中需要根据具体的文件存储服务来实现
        // 可能是本地存储、云存储（如阿里云OSS、AWS S3）等
        try {
            String originalFilename = file.getOriginalFilename();
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String fileType = getFileType(originalFilename);

            // TODO: 实现实际的文件上传逻辑
            String fileUrl = "https://example.com/files/" + fileId;

            FileInfoDTO fileInfo = new FileInfoDTO();
            fileInfo.setFileId(fileId);
            fileInfo.setFileName(originalFilename);
            fileInfo.setFileUrl(fileUrl);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(fileType);
            fileInfo.setUploadTime(LocalDateTime.now());

            return fileInfo;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new BusinessException("文件上传失败");
        }
    }

    private String getFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}