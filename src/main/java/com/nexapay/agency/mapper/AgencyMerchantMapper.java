package com.nexapay.agency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.dto.agency.MerchantListVO;
import com.nexapay.agency.entity.AgencyMerchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AgencyMerchantMapper extends BaseMapper<AgencyMerchant> {
    @Select("""
        SELECT 
            am.merchant_name,
            am.register_status,
            am.kyc_status,
            ml.create_time as first_contact_time,
            COALESCE(SUM(CASE WHEN amt.status = 90 THEN amt.amount ELSE 0 END), 0) as total_transaction_amount,
            MAX(amt.transaction_time) as last_transaction_time
        FROM agency_merchants am
        LEFT JOIN merchant_leads ml ON ml.email = am.email
        LEFT JOIN agency_merchant_transactions amt ON amt.merchant_id = am.merchant_id
        WHERE am.agency_id = #{agencyId}
        GROUP BY am.merchant_id, am.merchant_name, am.register_status, am.kyc_status, ml.create_time, am.create_time
        ORDER BY am.create_time DESC
        """)
    Page<MerchantListVO> selectMerchantListPage(Page<?> page, @Param("agencyId") Long agencyId);
}