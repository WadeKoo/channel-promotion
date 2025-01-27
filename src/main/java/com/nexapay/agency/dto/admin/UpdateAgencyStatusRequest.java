package com.nexapay.agency.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAgencyStatusRequest {
    @NotNull(message = "Agency ID cannot be null")
    private Long agencyUserId;

    @NotNull(message = "Status cannot be null")
    private Integer status;
}