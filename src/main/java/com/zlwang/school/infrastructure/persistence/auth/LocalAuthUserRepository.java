package com.zlwang.school.infrastructure.persistence.auth;

import com.zlwang.school.modules.auth.model.AuthPermission;
import com.zlwang.school.modules.auth.model.AuthUserAccount;
import com.zlwang.school.modules.auth.repository.AuthUserRepository;
import com.zlwang.school.infrastructure.persistence.local.LocalUserStore;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalAuthUserRepository implements AuthUserRepository {

    private static final List<AuthPermission> ADMIN_PERMISSIONS = List.of(
        menu(1, 0, "系统管理", "system", "/system", "Layout", "Setting", 10),
        menu(2, 1, "用户管理", "system:user", "/system/users", "system/user/index", "User", 11),
        menu(3, 1, "角色管理", "system:role", "/system/roles", "system/role/index", "UserRoundCog", 12),
        menu(4, 1, "权限管理", "system:permission", "/system/permissions", "system/permission/index", "ShieldCheck", 13),
        menu(5, 0, "CMS 管理", "cms", "/cms", "Layout", "Newspaper", 20),
        menu(6, 5, "栏目管理", "cms:column", "/cms/columns", "cms/column/index", "PanelTop", 21),
        menu(7, 5, "内容管理", "cms:content", "/cms/contents", "cms/content/index", "FileText", 22),
        menu(8, 5, "Banner 管理", "cms:banner", "/cms/banners", "cms/banner/index", "Images", 23),
        menu(9, 5, "媒体库管理", "cms:media", "/cms/media", "cms/media/index", "FolderOpen", 24),
        menu(10, 5, "友情链接管理", "cms:friend-link", "/cms/friend-links", "cms/friend-link/index", "Link", 25),
        menu(11, 5, "站点配置", "cms:site-config", "/cms/site-config", "cms/site-config/index", "SlidersHorizontal", 26),
        menu(12, 0, "日志管理", "log", "/logs", "Layout", "ClipboardList", 30),
        menu(13, 12, "操作日志", "log:operation", "/logs/operations", "log/operation/index", "ListChecks", 31),
        menu(14, 12, "登录日志", "log:login", "/logs/login", "log/login/index", "LogIn", 32),
        button(101, 2, "用户新增", "system:user:create", 101),
        button(102, 2, "用户编辑", "system:user:update", 102),
        button(103, 2, "用户删除", "system:user:delete", 103),
        button(104, 3, "角色维护", "system:role:manage", 104),
        button(105, 4, "权限维护", "system:permission:manage", 105),
        button(201, 6, "栏目维护", "cms:column:manage", 201),
        button(202, 7, "内容维护", "cms:content:manage", 202),
        button(203, 8, "Banner 维护", "cms:banner:manage", 203),
        button(204, 9, "媒体库维护", "cms:media:manage", 204),
        button(205, 10, "友情链接维护", "cms:friend-link:manage", 205),
        button(206, 11, "站点配置维护", "cms:site-config:manage", 206)
    );

    private final LocalUserStore localUserStore;

    public LocalAuthUserRepository(LocalUserStore localUserStore) {
        this.localUserStore = localUserStore;
    }

    @Override
    public Optional<AuthUserAccount> findByUsername(String username) {
        return localUserStore.findByUsername(username)
            .map(user -> {
                List<String> roleCodes = localUserStore.findRoleCodes(user.id());
                List<AuthPermission> permissions = roleCodes.contains("SUPER_ADMIN")
                    ? ADMIN_PERMISSIONS
                    : List.of();
                return new AuthUserAccount(
                    user.id(),
                    user.username(),
                    user.passwordHash(),
                    user.realName(),
                    user.avatarUrl(),
                    user.status() == 1,
                    roleCodes,
                    permissions
                );
            });
    }

    private static AuthPermission menu(
        long id,
        long parentId,
        String name,
        String code,
        String routePath,
        String componentPath,
        String icon,
        int sortNo
    ) {
        return new AuthPermission(
            id, parentId, name, code, "MENU", routePath, componentPath, icon, sortNo, true
        );
    }

    private static AuthPermission button(long id, long parentId, String name, String code, int sortNo) {
        return new AuthPermission(id, parentId, name, code, "BUTTON", null, null, null, sortNo, false);
    }
}
