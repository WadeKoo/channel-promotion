package com.nexapay.agency.controller.admin;

import com.nexapay.agency.common.R;
import com.nexapay.agency.service.MerchantSyncService;
import com.nexapay.agency.service.TransactionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync/agency")
@RequiredArgsConstructor
public class MerchantSyncController {
    private final MerchantSyncService merchantSyncService;
    private final TransactionSyncService transactionSyncService;

    @PostMapping("/merchant-info")
    public R syncMerchants() {
        merchantSyncService.syncMerchants();
        return R.success();
    }

    @PostMapping("/merchant-transactions")
    public R syncTransactions() {
        transactionSyncService.syncTransactions();
        return R.success();
    }
}
