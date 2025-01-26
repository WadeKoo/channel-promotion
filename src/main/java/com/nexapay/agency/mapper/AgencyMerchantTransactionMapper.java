package com.nexapay.agency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexapay.agency.entity.AgencyMerchantTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgencyMerchantTransactionMapper extends BaseMapper<AgencyMerchantTransaction> {
    @Select("SELECT COUNT(DISTINCT merchant_id) FROM agency_merchant_transactions " +
            "WHERE agency_id = #{agencyId} AND status = 90 " +
            "AND transaction_time BETWEEN #{startTime} AND #{endTime}")
    Long countActiveMerchants(@Param("agencyId") Long agencyId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM agency_merchant_transactions " +
            "WHERE agency_id = #{agencyId} AND status = 90 " +
            "AND transaction_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal sumTransactionAmount(@Param("agencyId") Long agencyId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    @Select("SELECT NOT EXISTS (SELECT 1 FROM agency_merchant_transactions " +
            "WHERE merchant_id = #{merchantId} AND status = 90 " +
            "AND create_time < (SELECT create_time FROM agency_merchant_transactions WHERE id = #{transactionId}))")
    boolean isFirstTransaction(@Param("merchantId") String merchantId,
                               @Param("transactionId") Long transactionId);

    @Select("SELECT * FROM agency_merchant_transactions " +
            "WHERE agency_id = #{agencyId} " +
            "AND transaction_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY transaction_time")
    List<AgencyMerchantTransaction> selectByTimeRange(@Param("agencyId") Long agencyId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}