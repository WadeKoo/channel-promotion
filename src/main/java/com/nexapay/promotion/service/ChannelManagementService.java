package com.nexapay.promotion.service;

import com.nexapay.promotion.common.R;
import com.nexapay.promotion.dto.admin.ChannelCommissionConfigRequest;

public interface ChannelManagementService {
    R getPendingVerifications(Integer page, Integer size);
    R getVerificationDetail(String id);
    R configCommission(ChannelCommissionConfigRequest request);
    R getChannelList(Integer page, Integer size);
}

