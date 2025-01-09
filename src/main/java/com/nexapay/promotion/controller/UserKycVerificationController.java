package com.nexapay.promotion.controller;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.*;
import com.nexapay.promotion.service.UserKYCVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyc/verification")
@RequiredArgsConstructor
public class UserKycVerificationController {
    private final UserKYCVerificationService userKYCVerificationService;

    @PostMapping("/init")
    public R<VerificationDTO> initVerification(@RequestParam String type) {
        return userKYCVerificationService.initVerification(type);
    }

    @GetMapping("/detail")
    public R<VerificationDTO> getVerification(@RequestParam Long id) {
        return userKYCVerificationService.getVerification(id);
    }

    @PostMapping("/update-personal")
    public R<VerificationDTO> updatePersonalInfo(
            @RequestParam Long id,
            @Validated @RequestBody PersonalInfoDTO personalInfo) {
        return userKYCVerificationService.updatePersonalInfo(id, personalInfo);
    }

    @PostMapping("/update-company")
    public R<VerificationDTO> updateCompanyInfo(
            @RequestParam Long id,
            @Validated @RequestBody CompanyInfoDTO companyInfo) {
        return userKYCVerificationService.updateCompanyInfo(id, companyInfo);
    }

    @PostMapping("/update-bank")
    public R<VerificationDTO> updateBankInfo(
            @RequestParam Long id,
            @Validated @RequestBody BankInfoDTO bankInfo) {
        return userKYCVerificationService.updateBankInfo(id, bankInfo);
    }

    @PostMapping("/upload-document")
    public R<VerificationDTO> uploadDocuments(
            @RequestParam Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String documentType) {
        return userKYCVerificationService.uploadDocument(id, file, documentType);
    }

    @PostMapping("/submit")
    public R<VerificationDTO> submit(@RequestParam Long id) {
        return userKYCVerificationService.submit(id);
    }

    @PostMapping("/update-agreement")
    public R<VerificationDTO> updateAgreement(
            @RequestParam Long id,
            @Validated @RequestBody AgreementInfoDTO agreementInfo) {
        return userKYCVerificationService.updateAgreement(id, agreementInfo);
    }
}