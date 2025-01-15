package com.nexapay.agency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexapay.agency.entity.AgencyKyc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AgencyKycMapper extends BaseMapper<AgencyKyc> {
    @Select("SELECT * FROM verification WHERE user_id = #{userId} AND status != #{status}")
    AgencyKyc findByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") String status);
}