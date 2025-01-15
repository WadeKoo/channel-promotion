package com.nexapay.agency.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResult {
    private final Long userId;
    private final boolean valid;
    private final String platform;

    public static TokenValidationResult invalid() {
        return new TokenValidationResult(null, false, null);
    }
}