package com.nexapay.agency.controller.admin;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.*;
import com.nexapay.agency.service.AgencyManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/agency")
@RequiredArgsConstructor
public class AgencyManagementController {

    private final AgencyManagementService agencyManagementService;

    @GetMapping("/kyc/list")
    public R getKycList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return agencyManagementService.getKycList(page, size);
    }

    @GetMapping("/kyc/detail")
    public R getKycDetail(@RequestParam String id) {
        return agencyManagementService.getKycDetail(id);
    }

    @PostMapping("/commission/config")
    public R configCommission(@RequestBody @Valid AgencyCommissionConfigRequest request) {
        return agencyManagementService.configCommission(request);
    }

    @GetMapping("/list")
    public R getAgencyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return agencyManagementService.getAgencyList(page, size);
    }

    @PostMapping("/commission/save")
    public R saveCommissionConfig(@RequestBody @Valid AgencyCommissionConfigRequest request) {
        return agencyManagementService.configCommission(request);
    }

    @PostMapping("/kyc/audit")
    public R auditKyc(@RequestBody @Valid AgencyKycAuditRequest request) {
        return agencyManagementService.auditKyc(request);
    }

    @PostMapping("/create")
    public R createAgency(@RequestBody @Valid CreateAgencyRequest request) {
        return agencyManagementService.createAgency(request);
    }

    @PostMapping("/send-invite-email")
    public R sendAgencyEmail(@RequestBody @Valid AgencyEmailRequest request) {
        return agencyManagementService.sendAgencyEmail(request);

    }
    @PostMapping("/status")
    public R updateAgencyStatus(@RequestBody @Valid UpdateAgencyStatusRequest request) {
        return agencyManagementService.updateAgencyStatus(request);
    }







}