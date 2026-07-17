package com.zlwang.school.modules.log.repository;

import com.zlwang.school.modules.log.model.LoginStatus;

public record CreateLoginLog(
    Long userId,
    String username,
    String loginIp,
    String userAgent,
    LoginStatus loginStatus,
    String failureReason
) {
}
