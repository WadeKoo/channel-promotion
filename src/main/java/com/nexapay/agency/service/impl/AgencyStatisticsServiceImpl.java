package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.*;

import com.nexapay.agency.dto.merchant.PageResponse;
import com.nexapay.agency.entity.AgencyCommissionConfig;
import com.nexapay.agency.entity.AgencyMerchant;
import com.nexapay.agency.entity.AgencyMerchantTransaction;
import com.nexapay.agency.mapper.AgencyCommissionConfigMapper;
import com.nexapay.agency.mapper.AgencyMerchantMapper;
import com.nexapay.agency.mapper.AgencyMerchantTransactionMapper;
import com.nexapay.agency.mapper.MerchantLeadMapper;
import com.nexapay.agency.service.AgencyStatisticsService;
import com.nexapay.agency.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgencyStatisticsServiceImpl implements AgencyStatisticsService {
    private final MerchantLeadMapper merchantLeadMapper;
    private final AgencyMerchantTransactionMapper transactionMapper;
    private final AgencyCommissionConfigMapper commissionConfigMapper;
    private final AgencyMerchantMapper merchantMapper;

    @Override
    public R<AgencyStatisticsVO> getAgencyStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            long intervalDays = ChronoUnit.DAYS.between(startTime, endTime);
            LocalDateTime previousStartTime = startTime.minusDays(intervalDays);
            LocalDateTime previousEndTime = endTime.minusDays(intervalDays);

            StatisticsData merchantLeads = getLeadsStatistics(currentUserId, startTime, endTime, previousStartTime, previousEndTime);
            StatisticsData registeredMerchants = getRegisteredStatistics(currentUserId, startTime, endTime, previousStartTime, previousEndTime);
            StatisticsData activeMerchants = getActiveStatistics(currentUserId, startTime, endTime, previousStartTime, previousEndTime);
            StatisticsData transactionAmount = getTransactionStatistics(currentUserId, startTime, endTime, previousStartTime, previousEndTime);
            StatisticsData commissionAmount = getCommissionStatistics(currentUserId, startTime, endTime, previousStartTime, previousEndTime);
            StatisticsData conversionRate = getConversionStatistics(activeMerchants, registeredMerchants);

            AgencyStatisticsVO result = AgencyStatisticsVO.builder()
                    .merchantLeads(merchantLeads)
                    .registeredMerchants(registeredMerchants)
                    .activeMerchants(activeMerchants)
                    .transactionAmount(transactionAmount)
                    .commissionAmount(commissionAmount)
                    .conversionRate(conversionRate)
                    .build();

            return R.success(result);
        } catch (Exception e) {
            log.error("Failed to get agency statistics", e);
            return R.error("获取渠道商统计数据失败" + e.getMessage());
        }
    }

    @Override
    public R<List<DailyTrendVO>> getDailyTrends(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            List<DailyTrendVO> dbResults = merchantLeadMapper.selectDailyTrends(startTime, endTime, currentUserId);

            Map<LocalDateTime, DailyTrendVO> trendMap = dbResults.stream()
                    .collect(Collectors.toMap(
                            DailyTrendVO::getDate,
                            trend -> trend
                    ));

            List<DailyTrendVO> result = new ArrayList<>();
            LocalDateTime current = startTime;

            while (!current.isAfter(endTime)) {
                if (trendMap.containsKey(current)) {
                    result.add(trendMap.get(current));
                } else {
                    DailyTrendVO emptyTrend = new DailyTrendVO();
                    emptyTrend.setDate(current);
                    emptyTrend.setRegisterCount(0L);
                    emptyTrend.setLeadCount(0L);
                    emptyTrend.setActiveCount(0L);
                    result.add(emptyTrend);
                }
                current = current.plusDays(1);
            }

            return R.success(result);
        } catch (Exception e) {
            log.error("Failed to get daily trends", e);
            return R.error("获取日常趋势数据失败");
        }
    }


    @Override
    public R<MerchantPageResponseDto<MerchantListVO>> getMerchantList(Integer page, Integer size) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Page<MerchantListVO> pageParam = new Page<>(page, size);
            Page<MerchantListVO> pageResult = merchantMapper.selectMerchantListPage(pageParam, currentUserId);
            return R.success(MerchantPageResponseDto.of(pageResult));
        } catch (Exception e) {
            log.error("Failed to get merchant list", e);
            return R.error("获取商户列表失败 " + e.getMessage());
        }
    }



    private StatisticsData getLeadsStatistics(Long currentUserId, LocalDateTime startTime, LocalDateTime endTime,
                                              LocalDateTime previousStartTime, LocalDateTime previousEndTime) {
        Long current = merchantLeadMapper.countByTimeRange(currentUserId, startTime, endTime);
        Long previous = merchantLeadMapper.countByTimeRange(currentUserId, previousStartTime, previousEndTime);
        return buildStatisticsData(current, previous);
    }


    private StatisticsData getRegisteredStatistics(Long agencyId, LocalDateTime startTime, LocalDateTime endTime,
                                                   LocalDateTime previousStartTime, LocalDateTime previousEndTime) {
        Long current = merchantLeadMapper.countRegisteredByTimeRange(agencyId, startTime, endTime);
        Long previous = merchantLeadMapper.countRegisteredByTimeRange(agencyId, previousStartTime, previousEndTime);
        return buildStatisticsData(current, previous);
    }

    private StatisticsData getActiveStatistics(Long agencyId, LocalDateTime startTime, LocalDateTime endTime,
                                               LocalDateTime previousStartTime, LocalDateTime previousEndTime) {
        Long current = transactionMapper.countActiveMerchants(agencyId, startTime, endTime);
        Long previous = transactionMapper.countActiveMerchants(agencyId, previousStartTime, previousEndTime);
        return buildStatisticsData(current, previous);
    }

    private StatisticsData getTransactionStatistics(Long agencyId, LocalDateTime startTime, LocalDateTime endTime,
                                                    LocalDateTime previousStartTime, LocalDateTime previousEndTime) {
        BigDecimal current = transactionMapper.sumTransactionAmount(agencyId, startTime, endTime);
        BigDecimal previous = transactionMapper.sumTransactionAmount(agencyId, previousStartTime, previousEndTime);
        return buildStatisticsData(current, previous);
    }

    private StatisticsData getCommissionStatistics(Long agencyId, LocalDateTime startTime, LocalDateTime endTime,
                                                   LocalDateTime previousStartTime, LocalDateTime previousEndTime) {
        AgencyCommissionConfig config = commissionConfigMapper.selectByAgencyId(agencyId);
        BigDecimal current = calculateCommission(agencyId, startTime, endTime, config);
        BigDecimal previous = calculateCommission(agencyId, previousStartTime, previousEndTime, config);
        return buildStatisticsData(current, previous);
    }

    private StatisticsData getConversionStatistics(StatisticsData active, StatisticsData registered) {
        BigDecimal currentRate = calculateConversionRate(active.getCurrent(), registered.getCurrent());
        BigDecimal previousRate = calculateConversionRate(active.getIncrement(), registered.getIncrement());
        return buildStatisticsData(currentRate, previousRate);
    }

    private BigDecimal calculateCommission(Long agencyId, LocalDateTime startTime, LocalDateTime endTime,
                                           AgencyCommissionConfig config) {
        List<AgencyMerchantTransaction> transactions = transactionMapper.selectByTimeRange(agencyId, startTime, endTime);
        BigDecimal totalCommission = BigDecimal.ZERO;
        Set<String> processedFirstOrders = new HashSet<>();

        for (AgencyMerchantTransaction trans : transactions) {
            if (trans.getStatus() != 90) continue;

            totalCommission = totalCommission.add(
                    trans.getAmount().multiply(config.getCommissionRate())
            );

            if (!processedFirstOrders.contains(trans.getMerchantId()) &&
                    transactionMapper.isFirstTransaction(trans.getMerchantId(), trans.getId())) {
                totalCommission = totalCommission.add(config.getFirstOrderBonus());
                processedFirstOrders.add(trans.getMerchantId());
            }
        }

        return totalCommission;
    }

    private BigDecimal calculateConversionRate(Long active, Long total) {
        if (total == null || total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(active)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private StatisticsData buildStatisticsData(Long current, Long previous) {
        return StatisticsData.builder()
                .current(current)
                .increment(current - previous)
                .build();
    }

    private StatisticsData buildStatisticsData(BigDecimal current, BigDecimal previous) {
        return StatisticsData.builder()
                .current(current.longValue())
                .increment(current.subtract(previous).longValue())
                .build();
    }
}