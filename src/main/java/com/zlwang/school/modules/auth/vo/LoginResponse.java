package com.zlwang.school.modules.auth.vo;

public record LoginResponse(
    String tokenType,
    String accessToken,
    long expiresIn,
    CurrentUserResponse user
) {
}
