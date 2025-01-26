package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexapay.agency.dto.admin.MerchantApiResponse;
import com.nexapay.agency.entity.AgencyMerchant;
import com.nexapay.agency.entity.MerchantLead;
import com.nexapay.agency.mapper.AgencyMerchantMapper;
import com.nexapay.agency.mapper.MerchantLeadMapper;
import com.nexapay.agency.service.MerchantSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantSyncServiceImpl implements MerchantSyncService {


    @Value("${nexapay.api.base-url}")
    private String merchantApiBaseUrl;

    private static final int REQUEST_INTERVAL_MS = 5000; // 5秒间隔

    private final MerchantLeadMapper merchantLeadMapper;
    private final AgencyMerchantMapper agencyMerchantMapper;
    private final RestTemplate restTemplate;

    @Override
    public void syncMerchants() {
        log.info("Starting merchant sync task");

        List<MerchantLead> leads = getUnprocessedLeads();

        for (MerchantLead lead : leads) {
            processSingleLead(lead);
            sleep();
        }

        log.info("Completed merchant sync task");
    }

    private List<MerchantLead> getUnprocessedLeads() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return merchantLeadMapper.selectList(new LambdaQueryWrapper<MerchantLead>()
                .isNull(MerchantLead::getLastSyncTime)
                .or()
                .lt(MerchantLead::getLastSyncTime, oneHourAgo)
                .last("LIMIT 100"));
    }

    private void processSingleLead(MerchantLead lead) {
        try {
            String url = merchantApiBaseUrl + "/api/v1/agent/merch/email/" + lead.getEmail();
            ResponseEntity<MerchantApiResponse> response = restTemplate.getForEntity(url, MerchantApiResponse.class);

            lead.setLastSyncTime(LocalDateTime.now());

            if (response.getBody().getSucc()) {
                processSuccessResponse(lead, response.getBody().getData());
            } else {
                processFailureResponse(lead, response.getBody().getMsg());
            }
        } catch (Exception e) {
            processError(lead, e);
        }

        merchantLeadMapper.updateById(lead);
    }

    private void sleep() {
        try {
            Thread.sleep(REQUEST_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep interrupted", e);
        }
    }

    private void processSuccessResponse(MerchantLead lead, MerchantApiResponse.MerchantData merchantData) {
        lead.setSyncStatus(1);
        lead.setSyncFailReason(null);

        AgencyMerchant agencyMerchant = buildAgencyMerchant(lead, merchantData);

        if (agencyMerchantMapper.update(agencyMerchant,
                new LambdaQueryWrapper<AgencyMerchant>()
                        .eq(AgencyMerchant::getMerchantId, merchantData.getMerchantId())) == 0) {
            agencyMerchant.setCreateTime(LocalDateTime.now());
            agencyMerchantMapper.insert(agencyMerchant);
        }
    }

    private AgencyMerchant buildAgencyMerchant(MerchantLead lead, MerchantApiResponse.MerchantData merchantData) {
        AgencyMerchant agencyMerchant = new AgencyMerchant();
        LocalDateTime now = LocalDateTime.now();

        agencyMerchant.setAgencyId(lead.getAgencyId());
        agencyMerchant.setMerchantId(merchantData.getMerchantId());
        agencyMerchant.setMerchantName(merchantData.getMerchantName());
        agencyMerchant.setEmail(merchantData.getEmail());
        agencyMerchant.setRegisterStatus(merchantData.getRegisterStatus());
        agencyMerchant.setKycStatus(merchantData.getKycStatus());
        agencyMerchant.setMerchantCreateTime(
                LocalDateTime.parse(merchantData.getCreateTime(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        agencyMerchant.setCreateTime(now);
        agencyMerchant.setUpdateTime(now);
        return agencyMerchant;
    }

    private void processFailureResponse(MerchantLead lead, String errorMessage) {
        lead.setSyncStatus(2);
        lead.setSyncFailReason(errorMessage);
    }

    private void processError(MerchantLead lead, Exception e) {
        log.error("Error syncing merchant lead: {}", lead.getEmail(), e);
        lead.setSyncStatus(2);
        lead.setSyncFailReason(e.getMessage());
    }
}