package com.nexapay.agency.dto.agency;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatisticsData {
    private Long current;          // 当前数值
    private Long increment;        // 增量
    private BigDecimal percentage; // 百分比（适用于转化率）
}