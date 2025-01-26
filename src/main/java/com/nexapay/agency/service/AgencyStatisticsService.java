package com.nexapay.agency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.AgencyStatisticsVO;
import com.nexapay.agency.dto.agency.DailyTrendVO;
import com.nexapay.agency.dto.agency.MerchantListVO;
import com.nexapay.agency.dto.agency.MerchantPageResponseDto;
import com.nexapay.agency.dto.merchant.PageResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AgencyStatisticsService {
    R<AgencyStatisticsVO> getAgencyStatistics(LocalDateTime startTime, LocalDateTime endTime);

    R<List<DailyTrendVO>> getDailyTrends(LocalDateTime startTime, LocalDateTime endTime);

    R<MerchantPageResponseDto<MerchantListVO>> getMerchantList(Integer page, Integer size);
}