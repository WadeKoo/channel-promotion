package com.nexapay.agency.dto.agency;

import com.nexapay.agency.entity.AgencyCommissionConfig;
import com.nexapay.agency.entity.AgencyUser;
import lombok.Data;

@Data
public class AgencyInfoDTO {
    private AgencyUser user;
    private KycDTO kyc;
    private AgencyCommissionConfig commission;
}

