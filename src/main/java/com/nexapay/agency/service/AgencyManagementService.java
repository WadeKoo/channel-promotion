package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.AgencyCommissionConfigRequest;
import com.nexapay.agency.dto.admin.AgencyEmailRequest;
import com.nexapay.agency.dto.admin.AgencyKycAuditRequest;
import com.nexapay.agency.dto.admin.CreateAgencyRequest;
import jakarta.validation.Valid;

public interface AgencyManagementService {
    R getKycList(Integer page, Integer size);
    R getKycDetail(String id);
    R configCommission(AgencyCommissionConfigRequest request);
    R getAgencyList(Integer page, Integer size);

    R auditKyc(@Valid AgencyKycAuditRequest request);

    R createAgency(CreateAgencyRequest request);
    R sendAgencyEmail(AgencyEmailRequest request);
}

