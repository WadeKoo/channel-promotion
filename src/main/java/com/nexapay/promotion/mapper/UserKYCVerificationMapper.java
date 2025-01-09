package com.nexapay.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexapay.promotion.entity.UserKYCVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserKYCVerificationMapper extends BaseMapper<UserKYCVerification> {
    @Select("SELECT * FROM verification WHERE user_id = #{userId} AND status != #{status}")
    UserKYCVerification findByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") String status);
}