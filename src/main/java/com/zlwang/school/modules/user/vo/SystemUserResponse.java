package com.zlwang.school.modules.user.vo;

import com.zlwang.school.modules.user.model.SystemUser;
import java.time.LocalDateTime;
import java.util.List;

public record SystemUserResponse(
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

    public SystemUserResponse {
        roleIds = List.copyOf(roleIds);
    }

    public static SystemUserResponse from(SystemUser user) {
        return new SystemUserResponse(
            user.id(),
            user.username(),
            user.realName(),
            user.avatarUrl(),
            user.email(),
            user.phone(),
            user.status(),
            user.remark(),
            user.createdAt(),
            user.updatedAt(),
            user.roleIds()
        );
    }
}
