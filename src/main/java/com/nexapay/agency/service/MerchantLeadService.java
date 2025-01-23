package com.nexapay.agency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.merchant.MerchantLeadDTO;
import com.nexapay.agency.dto.merchant.MerchantLeadRequest;
import com.nexapay.agency.dto.merchant.PageResponse;
import com.nexapay.agency.entity.AgencyUser;

public interface MerchantLeadService {
    R<AgencyUser> getAgencyByInviteCode(String inviteCode);
    R<MerchantLeadDTO> register(MerchantLeadRequest.Register request);
    R<MerchantLeadDTO> update(MerchantLeadRequest.Update request);
    R<PageResponse<MerchantLeadDTO>> list(Integer page, Integer size);
    R<MerchantLeadDTO> updateStatus(MerchantLeadRequest.UpdateStatus request);
}
