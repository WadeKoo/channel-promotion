package com.nexapay.promotion.controller.admin;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.admin.ChannelCommissionConfigRequest;
import com.nexapay.promotion.service.ChannelManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/channel")
@RequiredArgsConstructor
public class ChannelManagementController {

    private final ChannelManagementService channelManagementService;

    @GetMapping("/verifications")
    public R getPendingVerifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return channelManagementService.getPendingVerifications(page, size);
    }

    @GetMapping("/verification/detail")
    public R getVerificationDetail(@RequestParam String id) {
        return channelManagementService.getVerificationDetail(id);
    }

    @PostMapping("/commission/config")
    public R configCommission(@RequestBody @Valid ChannelCommissionConfigRequest request) {
        return channelManagementService.configCommission(request);
    }

    @GetMapping("/list")
    public R getChannelList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return channelManagementService.getChannelList(page, size);
    }

    @PostMapping("/commission/save")
    public R saveCommissionConfig(@RequestBody @Valid ChannelCommissionConfigRequest request) {
        return channelManagementService.configCommission(request);
    }






}