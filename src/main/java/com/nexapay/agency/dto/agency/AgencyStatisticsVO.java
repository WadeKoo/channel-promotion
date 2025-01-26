package com.nexapay.agency.dto.agency;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyStatisticsVO {
    private StatisticsData merchantLeads;      // 推广商户
    private StatisticsData registeredMerchants;// 注册商户
    private StatisticsData activeMerchants;    // 活跃商户
    private StatisticsData transactionAmount;  // 交易金额
    private StatisticsData commissionAmount;   // 佣金金额
    private StatisticsData conversionRate;     // 转化指标
}
