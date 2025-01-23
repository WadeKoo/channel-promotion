package com.nexapay.agency.dto.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private Page<T> page;
    private MerchantLeadStats stats;
}
