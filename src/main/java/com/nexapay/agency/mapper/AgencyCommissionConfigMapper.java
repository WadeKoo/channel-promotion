package com.nexapay.agency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexapay.agency.entity.AgencyCommissionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface AgencyCommissionConfigMapper extends BaseMapper<AgencyCommissionConfig> {
    @Select("SELECT * FROM agency_commission_config WHERE agency_user_id = #{agencyId}")
    AgencyCommissionConfig selectByAgencyId(@Param("agencyId") Long agencyId);
}