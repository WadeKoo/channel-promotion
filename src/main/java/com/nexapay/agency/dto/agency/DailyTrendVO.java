package com.nexapay.agency.dto.agency;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DailyTrendVO {
    private LocalDateTime date;
    private Long registerCount;
    private Long leadCount;
    private Long activeCount;
}
