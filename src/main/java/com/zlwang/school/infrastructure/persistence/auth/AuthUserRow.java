package com.zlwang.school.infrastructure.persistence.auth;

public record AuthUserRow(
    long id,
    String username,
    String password,
    String realName,
    String avatarUrl,
    int status
) {
}
