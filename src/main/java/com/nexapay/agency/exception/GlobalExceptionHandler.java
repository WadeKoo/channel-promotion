package com.nexapay.agency.exception;

import com.nexapay.agency.common.R;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理404资源未找到异常
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R handleNoResourceFoundException(NoResourceFoundException ex) {
        return R.error(404, "请求的资源不存在");
    }

    // 处理认证相关异常
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.OK)  // 返回200状态码
    public R handleAuthenticationException(Exception ex) {
        return R.error(40003, "请先登录");
    }

    // 处理 HTTP 方法不支持的异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return R.methodNotAllowed("不支持的 HTTP 方法: " + ex.getMessage());
    }

    // 处理请求体缺失的异常
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return R.error(400, "请求体不能为空");
    }

    // 处理参数验证失败的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        StringBuilder errorMsg = new StringBuilder();

        for (FieldError error : result.getFieldErrors()) {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        }

        return R.error(60003, errorMsg.toString().trim());
    }

    // 处理其他所有异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleAllException(Exception ex) {
        return R.error(500, "服务器内部错误: " + ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handleBusinessException(BusinessException ex) {
        return R.error(ex.getCode(), ex.getMessage());
    }
}