package com.nexapay.agency.controller;

import com.nexapay.agency.common.R;
import com.nexapay.agency.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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