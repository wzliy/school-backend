package com.zlwang.school.infrastructure.persistence.user;

import java.time.LocalDateTime;

public record SystemUserRow(
    long id,
    String username,
    String realName,
    String avatarUrl,
    String email,
    String phone,
    int status,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
