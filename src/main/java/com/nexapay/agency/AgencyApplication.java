package com.nexapay.agency;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.nexapay.agency.mapper")
@EnableTransactionManagement
public class AgencyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgencyApplication.class, args);
    }
}