package com.nexapay.agency.controller.admin;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AgencyCommissionConfigRequest;
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






}