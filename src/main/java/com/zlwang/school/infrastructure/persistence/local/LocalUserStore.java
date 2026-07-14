package com.zlwang.school.infrastructure.persistence.local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalUserStore {

    private static final String ADMIN_PASSWORD_HASH =
        "$2y$10$AT7CX..4P1ofYP8xM/j5cOXEDIvskr6yCAtYz5WHIXBm97Luq5IWa";

    private final AtomicLong userIdSequence = new AtomicLong(1L);
    private final Map<Long, LocalUser> users = new LinkedHashMap<>();
    private final Map<Long, LocalRole> roles = new LinkedHashMap<>();
    private final Map<Long, List<Long>> userRoleIds = new HashMap<>();

    public LocalUserStore() {
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
        roles.put(1L, new LocalRole(1L, "超级管理员", "SUPER_ADMIN", 1, 1));
        roles.put(2L, new LocalRole(2L, "网站管理员", "SITE_ADMIN", 2, 1));
        roles.put(3L, new LocalRole(3L, "内容编辑", "CONTENT_EDITOR", 3, 1));
        roles.put(4L, new LocalRole(4L, "内容审核员", "CONTENT_AUDITOR", 4, 1));
        roles.put(5L, new LocalRole(5L, "招生就业管理员", "RECRUIT_ADMIN", 5, 1));
        userRoleIds.put(1L, List.of(1L));
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

    public record LocalRole(long id, String name, String code, int sortNo, int status) {
    }
}
