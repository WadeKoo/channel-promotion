package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.agency.dto.admin.TransactionApiResponse;
import com.nexapay.agency.entity.AgencyMerchant;
import com.nexapay.agency.entity.AgencyMerchantTransaction;
import com.nexapay.agency.mapper.AgencyMerchantMapper;
import com.nexapay.agency.mapper.AgencyMerchantTransactionMapper;
import com.nexapay.agency.service.TransactionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionSyncServiceImpl implements TransactionSyncService {

    @Value("${nexapay.api.base-url}")
    private String apiBaseUrl;

    private static final int REQUEST_INTERVAL_MS = 5000;
    private static final int PAGE_SIZE = 1000;

    private final AgencyMerchantMapper agencyMerchantMapper;
    private final AgencyMerchantTransactionMapper transactionMapper;
    private final RestTemplate restTemplate;

    @Override
    public void syncTransactions() {
        log.info("Starting transaction sync task");
        List<AgencyMerchant> merchants = getUnprocessedMerchants();

        for (AgencyMerchant merchant : merchants) {
            processSingleMerchant(merchant);
            sleep();
        }

        log.info("Completed transaction sync task");
    }

    private List<AgencyMerchant> getUnprocessedMerchants() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return agencyMerchantMapper.selectList(new LambdaQueryWrapper<AgencyMerchant>()
                .isNull(AgencyMerchant::getLastTransSyncTime)
                .or()
                .lt(AgencyMerchant::getLastTransSyncTime, oneHourAgo));
    }

    private void processSingleMerchant(AgencyMerchant merchant) {
        try {
            LocalDateTime startTime = determineStartTime(merchant);
            LocalDateTime endTime = LocalDateTime.now();
            int pageNum = 1;

            while (true) {
                TransactionApiResponse response = fetchTransactions(merchant, startTime, endTime, pageNum);

                if (!response.getSucc() || response.getData().getList().isEmpty()) {
                    break;
                }

                saveTransactions(merchant, response.getData().getList());

                if (response.getData().getList().size() < PAGE_SIZE) {
                    break;
                }

                pageNum++;
                sleep();
            }

            updateMerchantSyncTime(merchant);

        } catch (Exception e) {
            log.error("Error syncing transactions for merchant: {}", merchant.getMerchantId(), e);
        }
    }

    private LocalDateTime determineStartTime(AgencyMerchant merchant) {
        AgencyMerchantTransaction lastTransaction = transactionMapper.selectOne(
                new LambdaQueryWrapper<AgencyMerchantTransaction>()
                        .eq(AgencyMerchantTransaction::getMerchantId, merchant.getMerchantId())
                        .orderByDesc(AgencyMerchantTransaction::getTransactionTime)
                        .last("LIMIT 1")
        );

        if (lastTransaction != null) {
            return lastTransaction.getTransactionTime().minusHours(1);
        }

        return merchant.getMerchantCreateTime();
    }

    private TransactionApiResponse fetchTransactions(AgencyMerchant merchant,
                                                     LocalDateTime startTime,
                                                     LocalDateTime endTime,
                                                     int pageNum) {
        String url = apiBaseUrl + "/api/v1/agent/merch/order/page";

        Map<String, Object> query = new HashMap<>();
        query.put("merchId", merchant.getMerchantId());
        query.put("startDate", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        query.put("endDate", endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, Object> request = new HashMap<>();
        request.put("pageNum", pageNum);
        request.put("pageSize", PAGE_SIZE);
        request.put("query", query);

        return restTemplate.postForObject(url, request, TransactionApiResponse.class);
    }

    private void saveTransactions(AgencyMerchant merchant, List<TransactionApiResponse.Transaction> transactions) {
        for (TransactionApiResponse.Transaction transaction : transactions) {
            AgencyMerchantTransaction entity = new AgencyMerchantTransaction();
            entity.setAgencyId(merchant.getAgencyId());
            entity.setMerchantId(merchant.getMerchantId());
            entity.setTransactionId(transaction.getTransactionId());
            entity.setAmount(new BigDecimal(transaction.getAmount()));
            entity.setCurrency(transaction.getCurrency());
            entity.setStatus(transaction.getStatus());
            entity.setTransactionTime(LocalDateTime.parse(transaction.getTransactionTime(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            LocalDateTime now = LocalDateTime.now();
            entity.setCreateTime(now);
            entity.setUpdateTime(now);

            try {
                transactionMapper.insert(entity);
            } catch (DuplicateKeyException e) {
                log.debug("Transaction already exists: {}", transaction.getTransactionId());
            }
        }
    }

    private void updateMerchantSyncTime(AgencyMerchant merchant) {
        merchant.setLastTransSyncTime(LocalDateTime.now());
        agencyMerchantMapper.updateById(merchant);
    }

    private void sleep() {
        try {
            Thread.sleep(REQUEST_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep interrupted", e);
        }
    }
}