package com.nexapay.agency.controller;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.AgencyStatisticsVO;
import com.nexapay.agency.dto.agency.DailyTrendVO;
import com.nexapay.agency.dto.agency.MerchantListVO;
import com.nexapay.agency.dto.agency.MerchantPageResponseDto;
import com.nexapay.agency.dto.merchant.PageResponse;
import com.nexapay.agency.service.AgencyStatisticsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/agency/statistics")
@RequiredArgsConstructor
public class AgencyStatisticsController {
    private final AgencyStatisticsService agencyStatisticsService;

    @GetMapping("/summary")
    public R<AgencyStatisticsVO> getStatistics(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return agencyStatisticsService.getAgencyStatistics(startTime, endTime);
    }



    @GetMapping("/trends")
    public R<List<DailyTrendVO>> getTrends(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return agencyStatisticsService.getDailyTrends(startTime, endTime);
    }

    @GetMapping("/merchants")
    public R<MerchantPageResponseDto<MerchantListVO>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return agencyStatisticsService.getMerchantList(page, size);
    }
}