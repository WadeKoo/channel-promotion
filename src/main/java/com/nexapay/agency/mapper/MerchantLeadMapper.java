package com.nexapay.agency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexapay.agency.dto.agency.DailyTrendVO;
import com.nexapay.agency.dto.agency.MerchantListVO;
import com.nexapay.agency.entity.MerchantLead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MerchantLeadMapper extends BaseMapper<MerchantLead> {
    @Select("SELECT COUNT(*) FROM merchant_leads " +
            "WHERE agency_id = #{agencyId} AND create_time BETWEEN #{startTime} AND #{endTime}")
    Long countByTimeRange(@Param("agencyId") Long agencyId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM merchant_leads " +
            "WHERE agency_id = #{agencyId} AND status = 2 " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    Long countRegisteredByTimeRange(@Param("agencyId") Long agencyId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    @Select("SELECT ml.id as merchant_id, ml.create_time as first_contact_time, " +
            "ml.status, COALESCE(SUM(amt.amount), 0) as total_transaction_amount " +
            "FROM merchant_leads ml " +
            "LEFT JOIN agency_merchant_transactions amt ON ml.id = amt.merchant_id AND amt.status = 90 " +
            "WHERE ml.agency_id = #{agencyId} " +
            "GROUP BY ml.id, ml.create_time, ml.status " +
            "ORDER BY ml.create_time DESC")
    Page<MerchantListVO> selectMerchantList(Page<MerchantLead> page, @Param("agencyId") Long agencyId);

    @Select("SELECT DATE(create_time) as date, COUNT(*) as lead_count, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as register_count, " +
            "COUNT(DISTINCT CASE WHEN EXISTS (" +
            "   SELECT 1 FROM agency_merchant_transactions t " +
            "   WHERE t.merchant_id = merchant_leads.id AND t.status = 90 " +
            "   AND t.create_time BETWEEN #{startTime} AND #{endTime}" +
            ") THEN merchant_leads.id END) as active_count " +
            "FROM merchant_leads " +
            "WHERE agency_id = #{agencyId} AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<DailyTrendVO> selectDailyTrends(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("agencyId") Long agencyId);
}