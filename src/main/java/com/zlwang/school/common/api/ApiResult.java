package com.zlwang.school.common.api;

import com.zlwang.school.common.exception.ErrorCode;

public record ApiResult<T>(String code, String msg, T data) {

    public static final String SUCCESS_CODE = "000000";
    public static final String SUCCESS_MESSAGE = "success";

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResult<T> failure(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.code(), errorCode.message(), null);
    }

    public static <T> ApiResult<T> failure(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.code(), message, null);
    }
}
