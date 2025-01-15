package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AgencyCommissionConfigRequest;

public interface AgencyManagementService {
    R getKycList(Integer page, Integer size);
    R getKycDetail(String id);
    R configCommission(AgencyCommissionConfigRequest request);
    R getAgencyList(Integer page, Integer size);
}

