package com.nexapay.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("channel_users")
public class ChannelUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String password;
    private Integer status;
    private Integer kycStatus;
    private String inviteCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}