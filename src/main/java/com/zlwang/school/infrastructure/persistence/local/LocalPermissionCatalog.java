package com.zlwang.school.infrastructure.persistence.local;

import com.zlwang.school.modules.auth.model.AuthPermission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalPermissionCatalog {

    private final AtomicLong permissionIdSequence = new AtomicLong(206L);
    private final Map<Long, LocalPermission> permissions = new LinkedHashMap<>();
    private final Set<String> reservedPermissionCodes = new HashSet<>();

    public LocalPermissionCatalog() {
        LocalDateTime now = LocalDateTime.now();
        add(menu(1, 0, "系统管理", "system", "/system", "Layout", "Setting", 10, now));
        add(menu(2, 1, "用户管理", "system:user", "/system/users", "system/user/index", "User", 11, now));
        add(menu(3, 1, "角色管理", "system:role", "/system/roles", "system/role/index", "UserRoundCog", 12, now));
        add(menu(4, 1, "权限管理", "system:permission", "/system/permissions", "system/permission/index", "ShieldCheck", 13, now));
        add(menu(5, 0, "CMS 管理", "cms", "/cms", "Layout", "Newspaper", 20, now));
        add(menu(6, 5, "栏目管理", "cms:column", "/cms/columns", "cms/column/index", "PanelTop", 21, now));
        add(menu(7, 5, "内容管理", "cms:content", "/cms/contents", "cms/content/index", "FileText", 22, now));
        add(menu(8, 5, "Banner 管理", "cms:banner", "/cms/banners", "cms/banner/index", "Images", 23, now));
        add(menu(9, 5, "媒体库管理", "cms:media", "/cms/media", "cms/media/index", "FolderOpen", 24, now));
        add(menu(10, 5, "友情链接管理", "cms:friend-link", "/cms/friend-links", "cms/friend-link/index", "Link", 25, now));
        add(menu(11, 5, "站点配置", "cms:site-config", "/cms/site-config", "cms/site-config/index", "SlidersHorizontal", 26, now));
        add(menu(12, 0, "日志管理", "log", "/logs", "Layout", "ClipboardList", 30, now));
        add(menu(13, 12, "操作日志", "log:operation", "/logs/operations", "log/operation/index", "ListChecks", 31, now));
        add(menu(14, 12, "登录日志", "log:login", "/logs/login", "log/login/index", "LogIn", 32, now));
        add(button(101, 2, "用户新增", "system:user:create", "POST", "/api/admin/users", 101, now));
        add(button(102, 2, "用户编辑", "system:user:update", "PUT", "/api/admin/users/{id}", 102, now));
        add(button(103, 2, "用户删除", "system:user:delete", "DELETE", "/api/admin/users/{id}", 103, now));
        add(button(104, 3, "角色维护", "system:role:manage", "*", "/api/admin/roles/**", 104, now));
        add(button(105, 4, "权限维护", "system:permission:manage", "*", "/api/admin/permissions/**", 105, now));
        add(button(201, 6, "栏目维护", "cms:column:manage", "*", "/api/admin/columns/**", 201, now));
        add(button(202, 7, "内容维护", "cms:content:manage", "*", "/api/admin/contents/**", 202, now));
        add(button(203, 8, "Banner 维护", "cms:banner:manage", "*", "/api/admin/banners/**", 203, now));
        add(button(204, 9, "媒体库维护", "cms:media:manage", "*", "/api/admin/media/**", 204, now));
        add(button(205, 10, "友情链接维护", "cms:friend-link:manage", "*", "/api/admin/friend-links/**", 205, now));
        add(button(206, 11, "站点配置维护", "cms:site-config:manage", "*", "/api/admin/site-config/**", 206, now));
    }

    public synchronized List<Long> allIds() {
        return activePermissions().stream().map(LocalPermission::id).toList();
    }

    public synchronized List<AuthPermission> findAllActive() {
        return activePermissions().stream().map(this::toAuthPermission).toList();
    }

    public synchronized long countExisting(List<Long> permissionIds) {
        Set<Long> existingIds = Set.copyOf(allIds());
        return permissionIds.stream().distinct().filter(existingIds::contains).count();
    }

    public synchronized List<AuthPermission> findByIds(List<Long> permissionIds) {
        Set<Long> selectedIds = Set.copyOf(permissionIds);
        return activePermissions().stream()
            .filter(permission -> selectedIds.contains(permission.id()))
            .map(this::toAuthPermission)
            .toList();
    }

    public synchronized List<LocalPermission> findAll() {
        return new ArrayList<>(permissions.values());
    }

    public synchronized Optional<LocalPermission> findById(long id) {
        return Optional.ofNullable(permissions.get(id));
    }

    public synchronized boolean codeExists(String permissionCode) {
        return reservedPermissionCodes.contains(permissionCode);
    }

    public synchronized long create(
        long parentId,
        String name,
        String code,
        String type,
        String routePath,
        String componentPath,
        String icon,
        String apiMethod,
        String apiPath,
        int sortNo,
        boolean visible,
        int status,
        String remark
    ) {
        if (codeExists(code)) {
            throw new IllegalStateException("权限编码已存在");
        }
        long id = permissionIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        add(new LocalPermission(
            id,
            parentId,
            name,
            code,
            type,
            routePath,
            componentPath,
            icon,
            apiMethod,
            apiPath,
            sortNo,
            visible,
            status,
            remark,
            now,
            now
        ));
        return id;
    }

    public synchronized boolean update(
        long id,
        long parentId,
        String name,
        String routePath,
        String componentPath,
        String icon,
        String apiMethod,
        String apiPath,
        int sortNo,
        boolean visible,
        int status,
        String remark
    ) {
        LocalPermission current = permissions.get(id);
        if (current == null) {
            return false;
        }
        permissions.put(id, new LocalPermission(
            id,
            parentId,
            name,
            current.code(),
            current.type(),
            routePath,
            componentPath,
            icon,
            apiMethod,
            apiPath,
            sortNo,
            visible,
            status,
            remark,
            current.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized long countChildren(long parentId) {
        return permissions.values().stream().filter(permission -> permission.parentId() == parentId).count();
    }

    public synchronized boolean delete(long id) {
        return permissions.remove(id) != null;
    }

    private void add(LocalPermission permission) {
        permissions.put(permission.id(), permission);
        reservedPermissionCodes.add(permission.code());
    }

    private List<LocalPermission> activePermissions() {
        return permissions.values().stream()
            .filter(permission -> permission.status() == 1)
            .sorted(Comparator.comparingInt(LocalPermission::sortNo).thenComparingLong(LocalPermission::id))
            .toList();
    }

    private AuthPermission toAuthPermission(LocalPermission permission) {
        return new AuthPermission(
            permission.id(),
            permission.parentId(),
            permission.name(),
            permission.code(),
            permission.type(),
            permission.routePath(),
            permission.componentPath(),
            permission.icon(),
            permission.sortNo(),
            permission.visible()
        );
    }

    private static LocalPermission menu(
        long id,
        long parentId,
        String name,
        String code,
        String routePath,
        String componentPath,
        String icon,
        int sortNo,
        LocalDateTime now
    ) {
        return new LocalPermission(
            id, parentId, name, code, "MENU", routePath, componentPath, icon,
            null, null, sortNo, true, 1, null, now, now
        );
    }

    private static LocalPermission button(
        long id,
        long parentId,
        String name,
        String code,
        String apiMethod,
        String apiPath,
        int sortNo,
        LocalDateTime now
    ) {
        return new LocalPermission(
            id, parentId, name, code, "BUTTON", null, null, null,
            apiMethod, apiPath, sortNo, false, 1, null, now, now
        );
    }

    public record LocalPermission(
        long id,
        long parentId,
        String name,
        String code,
        String type,
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
    }
}
