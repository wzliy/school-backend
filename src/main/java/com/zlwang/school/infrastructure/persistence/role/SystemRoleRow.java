package com.zlwang.school.infrastructure.persistence.role;

import java.time.LocalDateTime;

public record SystemRoleRow(
    long id,
    String roleName,
    String roleCode,
    int status,
    int sortNo,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
