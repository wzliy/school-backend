package com.zlwang.school.modules.auth.vo;

import com.zlwang.school.modules.auth.model.AuthPermission;
import com.zlwang.school.security.AuthenticatedUser;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CurrentUserResponse(
    long id,
    String username,
    String realName,
    String avatarUrl,
    List<String> roles,
    List<String> permissions,
    List<MenuResponse> menus
) {

    public CurrentUserResponse {
        roles = List.copyOf(roles);
        permissions = List.copyOf(permissions);
        menus = List.copyOf(menus);
    }

    public static CurrentUserResponse from(AuthenticatedUser user) {
        List<AuthPermission> menuPermissions = user.permissions().stream()
            .filter(permission -> "MENU".equals(permission.type()) && permission.visible())
            .sorted(Comparator.comparingInt(AuthPermission::sortNo).thenComparingLong(AuthPermission::id))
            .toList();
        Set<Long> menuIds = menuPermissions.stream()
            .map(AuthPermission::id)
            .collect(Collectors.toSet());
        List<MenuResponse> menus = menuPermissions.stream()
            .filter(permission -> permission.parentId() == 0 || !menuIds.contains(permission.parentId()))
            .map(permission -> toMenu(permission, menuPermissions))
            .toList();

        List<String> permissionCodes = user.permissions().stream()
            .map(AuthPermission::code)
            .distinct()
            .sorted()
            .toList();

        return new CurrentUserResponse(
            user.id(),
            user.getUsername(),
            user.realName(),
            user.avatarUrl(),
            user.roleCodes(),
            permissionCodes,
            menus
        );
    }

    private static MenuResponse toMenu(AuthPermission permission, List<AuthPermission> menuPermissions) {
        List<MenuResponse> children = menuPermissions.stream()
            .filter(candidate -> candidate.parentId() == permission.id())
            .map(candidate -> toMenu(candidate, menuPermissions))
            .toList();
        return new MenuResponse(
            permission.id(),
            permission.parentId(),
            permission.name(),
            permission.code(),
            permission.routePath(),
            permission.componentPath(),
            permission.icon(),
            permission.sortNo(),
            children
        );
    }
}
