package com.zlwang.school.modules.user.model;

import java.time.LocalDateTime;
import java.util.List;

public record SystemUser(
    long id,
    String username,
    String realName,
    String avatarUrl,
    String email,
    String phone,
    int status,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<Long> roleIds
) {

    public SystemUser {
        roleIds = List.copyOf(roleIds);
    }
}
