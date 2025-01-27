package com.nexapay.agency.service;

import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.admin.*;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface AgencyManagementService {
    R getKycList(Integer page, Integer size);
    R getKycDetail(String id);
    R configCommission(AgencyCommissionConfigRequest request);
    R getAgencyList(Integer page, Integer size);

    R auditKyc(@Valid AgencyKycAuditRequest request);

    R createAgency(CreateAgencyRequest request);
    R sendAgencyEmail(AgencyEmailRequest request);
    R updateAgencyStatus(UpdateAgencyStatusRequest request);


}

