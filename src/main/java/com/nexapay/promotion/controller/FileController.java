package com.nexapay.promotion.controller;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.FileInfoDTO;
import com.nexapay.promotion.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/common/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public R uploadFile(@RequestParam("file") MultipartFile file) {

        return fileService.uploadFile(file);
    }
}