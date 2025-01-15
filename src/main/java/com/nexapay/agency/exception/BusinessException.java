package com.nexapay.agency.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private int code;
    private String message;

    public BusinessException(String message) {
        this(60001, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}