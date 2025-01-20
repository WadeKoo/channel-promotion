package com.nexapay.agency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.merchant.MerchantLeadDTO;
import com.nexapay.agency.dto.merchant.MerchantLeadRequest;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.service.MerchantLeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agency/promotion/merchant-leads")
@RequiredArgsConstructor
public class MerchantLeadController {
    private final MerchantLeadService merchantService;

    @GetMapping("/agency-info")
    public R<AgencyUser> getAgencyByInviteCode(@RequestParam String inviteCode) {
        return merchantService.getAgencyByInviteCode(inviteCode);
    }


    @PostMapping("/register")
    public R<MerchantLeadDTO> register(@RequestBody MerchantLeadRequest.Register request) {
        return merchantService.register(request);
    }

    @PostMapping("/update")
    public R<MerchantLeadDTO> update(@RequestBody MerchantLeadRequest.Update request) {
        return merchantService.update(request);
    }


}