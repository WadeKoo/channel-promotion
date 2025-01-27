package com.nexapay.agency.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.merchant.MerchantLeadDTO;
import com.nexapay.agency.dto.merchant.MerchantLeadRequest;
import com.nexapay.agency.dto.merchant.PageResponse;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.service.MerchantLeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/merchant-lead")
@RequiredArgsConstructor
public class MerchantManagementController {
    private final MerchantLeadService merchantService;

    @GetMapping("/list")
    public R<PageResponse<MerchantLeadDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return merchantService.list(page, size);
    }

    @PostMapping("/sales")
    public R<MerchantLeadDTO> updateSales(@RequestBody MerchantLeadRequest.UpdateSales request) {
        return merchantService.updateSales(request);
    }


    @PostMapping("/status")
    public R<MerchantLeadDTO> updateStatus(@RequestBody MerchantLeadRequest.UpdateStatus request) {
        return merchantService.updateStatus(request);
    }
}