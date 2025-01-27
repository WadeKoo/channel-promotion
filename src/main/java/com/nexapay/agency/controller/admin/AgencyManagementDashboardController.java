package com.nexapay.agency.controller.admin;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AgencyManagementDashboardVO;
import com.nexapay.agency.service.AgencyManagementDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AgencyManagementDashboardController {
    private final AgencyManagementDashboardService agencyManagementDashboardService;

    @GetMapping("/statistics")
    public R<AgencyManagementDashboardVO> getDashboardStatistics(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return R.success(agencyManagementDashboardService.getDashboardStatistics(startTime, endTime));
    }
}