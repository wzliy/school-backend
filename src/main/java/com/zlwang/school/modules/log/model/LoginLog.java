package com.zlwang.school.modules.log.model;

import java.time.LocalDateTime;

public record LoginLog(
    long id,
    Long userId,
    String username,
    String loginIp,
    String userAgent,
    LoginStatus loginStatus,
    String failureReason,
    LocalDateTime createdAt
) {
}
