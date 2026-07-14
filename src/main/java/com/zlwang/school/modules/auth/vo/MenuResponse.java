package com.zlwang.school.modules.auth.vo;

import java.util.List;

public record MenuResponse(
    long id,
    long parentId,
    String name,
    String permissionCode,
    String routePath,
    String componentPath,
    String icon,
    int sortNo,
    List<MenuResponse> children
) {

    public MenuResponse {
        children = List.copyOf(children);
    }
}
