package com.nexapay.agency.controller;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.KycRequest;
import com.nexapay.agency.dto.agency.KycDTO;
import com.nexapay.agency.service.AgencyKycService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agency/kyc")
@RequiredArgsConstructor
public class AgencyKycController {
    private final AgencyKycService agencyKycService;

    @PostMapping("/init")
    public R<KycDTO> initKyc(@RequestBody KycRequest.Init request) {
        return agencyKycService.initKyc(request.getType());
    }

    @GetMapping("/detail")
    public R<KycDTO> getKyc() {
        return agencyKycService.getKyc();
    }

    @PostMapping("/update-personal")
    public R<KycDTO> updatePersonalInfo(@RequestBody KycRequest.UpdatePersonal request) {
        return agencyKycService.updatePersonalInfo(request.getId(), request.getPersonalInfo());
    }

    @PostMapping("/update-company")
    public R<KycDTO> updateCompanyInfo(@RequestBody KycRequest.UpdateCompany request) {
        return agencyKycService.updateCompanyInfo(request.getId(), request.getCompanyInfo());
    }

    @PostMapping("/update-bank")
    public R<KycDTO> updateBankInfo(@RequestBody KycRequest.UpdateBank request) {
        return agencyKycService.updateBankInfo(request.getId(), request.getBankInfo());
    }

    @PostMapping("/update-document")
    public R<KycDTO> updateDocumentAndSubmit(@RequestBody KycRequest.UpdateDocument request) {
        return agencyKycService.updateDocumentAndSubmit(request.getId(), request.getDocuments());
    }

    @PostMapping("/update-agreement")
    public R updateAgreement(@RequestBody KycRequest.UpdateAgreement request) {
        return agencyKycService.updateAgreement(request.getId(), request.getAgreementInfo());
    }


}