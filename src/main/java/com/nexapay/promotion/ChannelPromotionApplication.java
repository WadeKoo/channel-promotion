package com.nexapay.promotion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.nexapay.promotion.mapper")
@EnableTransactionManagement
public class ChannelPromotionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChannelPromotionApplication.class, args);
    }
}