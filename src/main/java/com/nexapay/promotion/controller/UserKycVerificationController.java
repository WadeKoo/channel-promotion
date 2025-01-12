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
    public R<VerificationDTO> initVerification(@RequestBody KycVerificationRequest.Init request) {
        return userKYCVerificationService.initVerification(request.getType());
    }

    @GetMapping("/detail")
    public R<VerificationDTO> getVerification() {
        return userKYCVerificationService.getVerification();
    }

    @PostMapping("/update-personal")
    public R<VerificationDTO> updatePersonalInfo(@RequestBody KycVerificationRequest.UpdatePersonal request) {
        return userKYCVerificationService.updatePersonalInfo(request.getId(), request.getPersonalInfo());
    }

    @PostMapping("/update-company")
    public R<VerificationDTO> updateCompanyInfo(@RequestBody KycVerificationRequest.UpdateCompany request) {
        return userKYCVerificationService.updateCompanyInfo(request.getId(), request.getCompanyInfo());
    }

    @PostMapping("/update-bank")
    public R<VerificationDTO> updateBankInfo(@RequestBody KycVerificationRequest.UpdateBank request) {
        return userKYCVerificationService.updateBankInfo(request.getId(), request.getBankInfo());
    }

    @PostMapping("/update-document")
    public R<VerificationDTO> updateDocumentAndSubmit(@RequestBody KycVerificationRequest.UpdateDocument request) {
        return userKYCVerificationService.updateDocumentAndSubmit(request.getId(), request.getDocuments());
    }

    @PostMapping("/update-agreement")
    public R<VerificationDTO> updateAgreement(@RequestBody KycVerificationRequest.UpdateAgreement request) {
        return userKYCVerificationService.updateAgreement(request.getId(), request.getAgreementInfo());
    }
}