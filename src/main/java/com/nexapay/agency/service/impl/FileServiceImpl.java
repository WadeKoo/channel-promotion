package com.nexapay.agency.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.nexapay.agency.common.R;
import com.nexapay.agency.common.config.OssProperties;
import com.nexapay.agency.exception.BusinessException;
import com.nexapay.agency.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssProperties ossProperties;

    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    @Override
    public R<String> uploadFile(MultipartFile file) {
        log.info("Starting file upload process. Original filename: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            validateFile(file);

            // 获取文件后缀
            String extension = getFileType(file.getOriginalFilename());
            String fileId = UUID.randomUUID().toString().replace("-", "");
            // 生成包含后缀的对象名
            String objectName = generateObjectName(fileId, extension);
            log.info("Generated object name for OSS: {}", objectName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            log.info("Preparing to upload file to OSS...");
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    metadata
            );

            ossClient.putObject(putObjectRequest);
            log.info("File successfully uploaded to OSS");

            // 构建标准的OSS URL
            String fileUrl = String.format("https://%s.%s/%s",
                    ossProperties.getBucketName(),
                    ossProperties.getEndpoint(),
                    objectName);

            log.info("File upload completed. Generated URL: {}", fileUrl);
            return R.success(fileUrl);

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        log.debug("Starting file validation...");

        if (file == null || file.isEmpty()) {
            log.error("Validation failed: File is null or empty");
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("Validation failed: File size {} exceeds maximum allowed size {}",
                    file.getSize(), MAX_FILE_SIZE);
            throw new BusinessException("文件大小不能超过30MB");
        }

        String fileType = getFileType(file.getOriginalFilename());
        log.debug("File type detected: {}", fileType);

        boolean isAllowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(fileType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            log.error("Validation failed: File type {} is not allowed", fileType);
            throw new BusinessException("不支持的文件类型");
        }
    }

    // 修改后的generateObjectName方法，添加文件后缀
    private String generateObjectName(String fileId, String extension) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("images/%s/%s.%s", datePrefix, fileId, extension);
    }

    private String getFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}