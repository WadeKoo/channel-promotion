package com.nexapay.agency.dto.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
public class MerchantLeadStats {
    private Long totalLeads;
    private Long newLeadsThisMonth;
    private Long inProgressLeads;
    private Long convertedLeads;
}
