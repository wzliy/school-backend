package com.zlwang.school.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SUCCESS("000000", "success", HttpStatus.OK),
    PARAM_VALIDATION_FAILED("A0400", "参数校验失败", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("A0401", "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("A0403", "无权限访问", HttpStatus.FORBIDDEN),
    NOT_FOUND("A0404", "资源不存在", HttpStatus.NOT_FOUND),
    BUSINESS_ERROR("B0001", "业务处理失败", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR("B0500", "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
