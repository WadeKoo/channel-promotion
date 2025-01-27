package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.agency.dto.admin.AgencyManagementDashboardVO;
import com.nexapay.agency.entity.*;
import com.nexapay.agency.mapper.*;
import com.nexapay.agency.service.AgencyManagementDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyManagementDashboardServiceImpl implements AgencyManagementDashboardService {
    private final AgencyUserMapper agencyUserMapper;
    private final MerchantLeadMapper merchantLeadMapper;
    private final AgencyMerchantMapper agencyMerchantMapper;
    private final AgencyMerchantTransactionMapper transactionMapper;
    private final AgencyCommissionConfigMapper commissionConfigMapper;

    @Override
    public AgencyManagementDashboardVO getDashboardStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        AgencyManagementDashboardVO vo = new AgencyManagementDashboardVO();

        // 1. 总渠道数量 - 在时间区间内注册的渠道
        LambdaQueryWrapper<AgencyUser> agencyQuery = new LambdaQueryWrapper<>();
        agencyQuery.eq(AgencyUser::getStatus, 1)
                .between(AgencyUser::getCreateTime, startTime, endTime);
        vo.setTotalAgencies(agencyUserMapper.selectCount(agencyQuery));

        // 2. 推广商户总数 - 在时间区间内推广的商户
        Set<String> uniqueEmails = new HashSet<>();

        LambdaQueryWrapper<MerchantLead> leadQuery = new LambdaQueryWrapper<>();
        leadQuery.between(MerchantLead::getCreateTime, startTime, endTime);
        uniqueEmails.addAll(merchantLeadMapper.selectList(leadQuery)
                .stream()
                .map(MerchantLead::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        LambdaQueryWrapper<AgencyMerchant> merchantQuery = new LambdaQueryWrapper<>();
        merchantQuery.between(AgencyMerchant::getCreateTime, startTime, endTime);
        uniqueEmails.addAll(agencyMerchantMapper.selectList(merchantQuery)
                .stream()
                .map(AgencyMerchant::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        vo.setTotalPromotedMerchants((long) uniqueEmails.size());

        // 3. 注册商户数量 - 在时间区间内注册的商户
        LambdaQueryWrapper<AgencyMerchant> registerQuery = new LambdaQueryWrapper<>();
        registerQuery.between(AgencyMerchant::getCreateTime, startTime, endTime);
        vo.setRegisteredMerchants(agencyMerchantMapper.selectCount(registerQuery));

        // 4. 活跃商户数 - 在时间区间内有交易的商户
        LambdaQueryWrapper<AgencyMerchantTransaction> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.eq(AgencyMerchantTransaction::getStatus, 90)
                .between(AgencyMerchantTransaction::getTransactionTime, startTime, endTime)
                .select(AgencyMerchantTransaction::getMerchantId);
        List<String> merchantIds = transactionMapper.selectList(activeQuery)
                .stream()
                .map(AgencyMerchantTransaction::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        vo.setActiveMerchants((long) merchantIds.size());

        // 5. 总交易金额 - 时间区间内的交易
        LambdaQueryWrapper<AgencyMerchantTransaction> amountQuery = new LambdaQueryWrapper<>();
        amountQuery.eq(AgencyMerchantTransaction::getStatus, 90)
                .between(AgencyMerchantTransaction::getTransactionTime, startTime, endTime);
        List<AgencyMerchantTransaction> transactions = transactionMapper.selectList(amountQuery);

        BigDecimal totalAmount = transactions.stream()
                .map(AgencyMerchantTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalTransactionAmount(totalAmount);

        // 6. 佣金计算 - 时间区间内的交易佣金
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal HUNDRED = new BigDecimal("100");

        // 获取每个商户在该时间区间之前的首单时间
        Map<String, LocalDateTime> firstOrderTimeMap = new HashMap<>();
        for (AgencyMerchantTransaction transaction : transactions) {
            String key = transaction.getAgencyId() + "-" + transaction.getMerchantId();
            if (!firstOrderTimeMap.containsKey(key) ||
                    transaction.getTransactionTime().isBefore(firstOrderTimeMap.get(key))) {
                firstOrderTimeMap.put(key, transaction.getTransactionTime());
            }
        }

        for (AgencyMerchantTransaction transaction : transactions) {
            AgencyCommissionConfig config = commissionConfigMapper.selectOne(
                    new LambdaQueryWrapper<AgencyCommissionConfig>()
                            .eq(AgencyCommissionConfig::getAgencyUserId, transaction.getAgencyId())
                            .eq(AgencyCommissionConfig::getStatus, 1)
            );

            if (config != null) {
                // 基础佣金
                BigDecimal commission = transaction.getAmount()
                        .multiply(config.getCommissionRate())
                        .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP);

                // 首单奖励
                String key = transaction.getAgencyId() + "-" + transaction.getMerchantId();
                if (firstOrderTimeMap.get(key).equals(transaction.getTransactionTime())) {
                    commission = commission.add(config.getFirstOrderBonus());
                }

                totalCommission = totalCommission.add(commission);
            }
        }

        vo.setTotalCommission(totalCommission);
        return vo;
    }
}