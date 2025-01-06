package com.nexapay.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexapay.promotion.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}