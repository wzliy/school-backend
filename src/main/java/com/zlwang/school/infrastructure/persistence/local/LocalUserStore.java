package com.zlwang.school.infrastructure.persistence.local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
public class LocalUserStore {

    private static final String ADMIN_PASSWORD_HASH =
        "{bcrypt}$2y$10$AT7CX..4P1ofYP8xM/j5cOXEDIvskr6yCAtYz5WHIXBm97Luq5IWa";

    private final AtomicLong userIdSequence = new AtomicLong(1L);
    private final AtomicLong roleIdSequence = new AtomicLong(5L);
    private final Map<Long, LocalUser> users = new LinkedHashMap<>();
    private final Map<Long, LocalRole> roles = new LinkedHashMap<>();
    private final Map<Long, List<Long>> userRoleIds = new HashMap<>();
    private final Map<Long, List<Long>> rolePermissionIds = new HashMap<>();
    private final Set<String> reservedRoleCodes = new HashSet<>();

    public LocalUserStore(LocalPermissionCatalog permissionCatalog) {
        LocalDateTime now = LocalDateTime.now();
        users.put(1L, new LocalUser(
            1L,
            "admin",
            ADMIN_PASSWORD_HASH,
            "超级管理员",
            null,
            "admin@example.com",
            null,
            1,
            "本地开发管理员",
            now,
            now
        ));
        roles.put(1L, role(1L, "超级管理员", "SUPER_ADMIN", 1, "拥有系统全部权限", now));
        roles.put(2L, role(2L, "网站管理员", "SITE_ADMIN", 2, "管理官网内容", now));
        roles.put(3L, role(3L, "内容编辑", "CONTENT_EDITOR", 3, "维护指定栏目内容", now));
        roles.put(4L, role(4L, "内容审核员", "CONTENT_AUDITOR", 4, "预留审核角色", now));
        roles.put(5L, role(5L, "招生就业管理员", "RECRUIT_ADMIN", 5, "维护专题站内容", now));
        roles.values().stream().map(LocalRole::code).forEach(reservedRoleCodes::add);
        userRoleIds.put(1L, List.of(1L));
        rolePermissionIds.put(1L, permissionCatalog.allIds());
    }

    public synchronized Optional<LocalUser> findByUsername(String username) {
        return users.values().stream()
            .filter(user -> user.username().equalsIgnoreCase(username))
            .findFirst();
    }

    public synchronized Optional<LocalUser> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public synchronized List<LocalUser> findAll() {
        return new ArrayList<>(users.values());
    }

    public synchronized boolean usernameExists(String username) {
        return users.values().stream().anyMatch(user -> user.username().equalsIgnoreCase(username));
    }

    public synchronized long create(
        String username,
        String passwordHash,
        String realName,
        String email,
        String phone,
        int status,
        String remark,
        List<Long> roleIds
    ) {
        if (usernameExists(username)) {
            throw new IllegalStateException("用户名已存在");
        }
        long id = userIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        users.put(id, new LocalUser(
            id,
            username,
            passwordHash,
            realName,
            null,
            email,
            phone,
            status,
            remark,
            now,
            now
        ));
        userRoleIds.put(id, List.copyOf(roleIds));
        return id;
    }

    public synchronized boolean update(
        long id,
        String realName,
        String email,
        String phone,
        String remark
    ) {
        LocalUser current = users.get(id);
        if (current == null) {
            return false;
        }
        users.put(id, new LocalUser(
            current.id(),
            current.username(),
            current.passwordHash(),
            realName,
            current.avatarUrl(),
            email,
            phone,
            current.status(),
            remark,
            current.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized boolean updateStatus(long id, int status) {
        LocalUser current = users.get(id);
        if (current == null) {
            return false;
        }
        users.put(id, new LocalUser(
            current.id(),
            current.username(),
            current.passwordHash(),
            current.realName(),
            current.avatarUrl(),
            current.email(),
            current.phone(),
            status,
            current.remark(),
            current.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized boolean updatePassword(long id, String passwordHash) {
        LocalUser current = users.get(id);
        if (current == null) {
            return false;
        }
        users.put(id, new LocalUser(
            current.id(),
            current.username(),
            passwordHash,
            current.realName(),
            current.avatarUrl(),
            current.email(),
            current.phone(),
            current.status(),
            current.remark(),
            current.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized boolean delete(long id) {
        if (users.remove(id) == null) {
            return false;
        }
        userRoleIds.remove(id);
        return true;
    }

    public synchronized boolean replaceRoles(long userId, List<Long> roleIds) {
        if (!users.containsKey(userId)) {
            return false;
        }
        userRoleIds.put(userId, List.copyOf(roleIds));
        return true;
    }

    public synchronized List<Long> findRoleIds(long userId) {
        return List.copyOf(userRoleIds.getOrDefault(userId, List.of()));
    }

    public synchronized List<String> findRoleCodes(long userId) {
        return findRoleIds(userId).stream()
            .map(roles::get)
            .filter(role -> role != null && role.status() == 1)
            .sorted((left, right) -> Long.compare(left.id(), right.id()))
            .map(LocalRole::code)
            .toList();
    }

    public synchronized List<LocalRole> findRoles() {
        return roles.values().stream()
            .filter(role -> role.status() == 1)
            .sorted((left, right) -> Integer.compare(left.sortNo(), right.sortNo()))
            .toList();
    }

    public synchronized List<LocalRole> findAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public synchronized Optional<LocalRole> findRoleById(long id) {
        return Optional.ofNullable(roles.get(id));
    }

    public synchronized boolean roleCodeExists(String roleCode) {
        return reservedRoleCodes.stream().anyMatch(code -> code.equalsIgnoreCase(roleCode));
    }

    public synchronized long createRole(
        String name,
        String code,
        int status,
        int sortNo,
        String remark
    ) {
        if (roleCodeExists(code)) {
            throw new IllegalStateException("角色编码已存在");
        }
        long id = roleIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        roles.put(id, new LocalRole(id, name, code, status, sortNo, remark, now, now));
        reservedRoleCodes.add(code);
        rolePermissionIds.put(id, List.of());
        return id;
    }

    public synchronized boolean updateRole(
        long id,
        String name,
        int status,
        int sortNo,
        String remark
    ) {
        LocalRole current = roles.get(id);
        if (current == null) {
            return false;
        }
        roles.put(id, new LocalRole(
            current.id(),
            name,
            current.code(),
            status,
            sortNo,
            remark,
            current.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized boolean replaceRolePermissions(long roleId, List<Long> permissionIds) {
        if (!roles.containsKey(roleId)) {
            return false;
        }
        rolePermissionIds.put(roleId, List.copyOf(permissionIds));
        return true;
    }

    public synchronized List<Long> findPermissionIdsByRoleId(long roleId) {
        return List.copyOf(rolePermissionIds.getOrDefault(roleId, List.of()));
    }

    public synchronized List<Long> findPermissionIdsByUserId(long userId) {
        return findRoleIds(userId).stream()
            .map(roles::get)
            .filter(role -> role != null && role.status() == 1)
            .flatMap(role -> findPermissionIdsByRoleId(role.id()).stream())
            .distinct()
            .sorted()
            .toList();
    }

    public synchronized long countUsersByRoleId(long roleId) {
        return userRoleIds.values().stream().filter(roleIds -> roleIds.contains(roleId)).count();
    }

    public synchronized long countNonSuperRolesByPermissionId(long permissionId) {
        return rolePermissionIds.entrySet().stream()
            .filter(entry -> {
                LocalRole role = roles.get(entry.getKey());
                return role != null && !"SUPER_ADMIN".equals(role.code());
            })
            .filter(entry -> entry.getValue().contains(permissionId))
            .count();
    }

    public synchronized void removePermissionFromRoles(long permissionId) {
        rolePermissionIds.replaceAll((roleId, permissionIds) -> permissionIds.stream()
            .filter(id -> id != permissionId)
            .toList());
    }

    public synchronized boolean deleteRole(long roleId) {
        if (roles.remove(roleId) == null) {
            return false;
        }
        rolePermissionIds.remove(roleId);
        return true;
    }

    public synchronized long countRoles(List<Long> roleIds) {
        return roleIds.stream().distinct().filter(roles::containsKey).count();
    }

    public record LocalUser(
        long id,
        String username,
        String passwordHash,
        String realName,
        String avatarUrl,
        String email,
        String phone,
        int status,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    private static LocalRole role(
        long id,
        String name,
        String code,
        int sortNo,
        String remark,
        LocalDateTime now
    ) {
        return new LocalRole(id, name, code, 1, sortNo, remark, now, now);
    }

    public record LocalRole(
        long id,
        String name,
        String code,
        int status,
        int sortNo,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}
