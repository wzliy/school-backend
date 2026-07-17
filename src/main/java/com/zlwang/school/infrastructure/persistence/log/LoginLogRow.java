package com.zlwang.school.infrastructure.persistence.log;

import java.time.LocalDateTime;

public record LoginLogRow(
    long id,
    Long userId,
    String username,
    String loginIp,
    String userAgent,
    String loginStatus,
    String failureReason,
    LocalDateTime createdAt
) {
}
