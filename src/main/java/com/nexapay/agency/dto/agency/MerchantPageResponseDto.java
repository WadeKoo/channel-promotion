package com.nexapay.agency.dto.agency;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class MerchantPageResponseDto<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;

    public static <T> MerchantPageResponseDto<T> of(Page<T> page) {
        MerchantPageResponseDto<T> response = new MerchantPageResponseDto<>();
        response.setRecords(page.getRecords());
        response.setTotal(page.getTotal());
        response.setSize(page.getSize());
        response.setCurrent(page.getCurrent());
        return response;
    }
}