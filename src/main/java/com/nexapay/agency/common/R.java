package com.nexapay.agency.common;

import lombok.Data;

@Data
public class R<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> error(String message) {
        return error(500, message);
    }

    // 常用的错误响应
    public static <T> R<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> R<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> R<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> R<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> R<T> methodNotAllowed(String message) {
        return error(405, message);
    }

    public static <T> R<T> serverError(String message) {
        return error(500, message);
    }
}