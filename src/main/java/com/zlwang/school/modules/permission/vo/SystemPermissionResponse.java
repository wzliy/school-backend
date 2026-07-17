package com.zlwang.school.modules.permission.vo;

import com.zlwang.school.modules.permission.model.SystemPermission;
import java.time.LocalDateTime;

public record SystemPermissionResponse(
    long id,
    long parentId,
    String permissionName,
    String permissionCode,
    String permissionType,
    String routePath,
    String componentPath,
    String icon,
    String apiMethod,
    String apiPath,
    int sortNo,
    boolean visible,
    int status,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static SystemPermissionResponse from(SystemPermission permission) {
        return new SystemPermissionResponse(
            permission.id(),
            permission.parentId(),
            permission.permissionName(),
            permission.permissionCode(),
            permission.permissionType(),
            permission.routePath(),
            permission.componentPath(),
            permission.icon(),
            permission.apiMethod(),
            permission.apiPath(),
            permission.sortNo(),
            permission.visible(),
            permission.status(),
            permission.remark(),
            permission.createdAt(),
            permission.updatedAt()
        );
    }
}
