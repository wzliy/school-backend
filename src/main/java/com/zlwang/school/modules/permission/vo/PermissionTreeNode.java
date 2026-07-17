package com.zlwang.school.modules.permission.vo;

import com.zlwang.school.modules.permission.model.SystemPermission;
import java.util.List;

public record PermissionTreeNode(
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
    List<PermissionTreeNode> children
) {

    public PermissionTreeNode {
        children = List.copyOf(children);
    }

    public static PermissionTreeNode from(SystemPermission permission, List<PermissionTreeNode> children) {
        return new PermissionTreeNode(
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
            children
        );
    }
}
