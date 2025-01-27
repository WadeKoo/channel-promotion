package com.nexapay.agency.service;

import com.nexapay.agency.dto.admin.AgencyManagementDashboardVO;

import java.time.LocalDateTime;

public interface AgencyManagementDashboardService {
    AgencyManagementDashboardVO getDashboardStatistics(LocalDateTime startTime, LocalDateTime endTime);
}

