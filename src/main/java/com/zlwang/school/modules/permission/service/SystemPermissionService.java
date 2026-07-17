package com.zlwang.school.modules.permission.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.permission.dto.CreatePermissionRequest;
import com.zlwang.school.modules.permission.dto.UpdatePermissionRequest;
import com.zlwang.school.modules.permission.model.SystemPermission;
import com.zlwang.school.modules.permission.repository.CreateSystemPermission;
import com.zlwang.school.modules.permission.repository.SystemPermissionRepository;
import com.zlwang.school.modules.permission.repository.UpdateSystemPermission;
import com.zlwang.school.modules.permission.vo.PermissionTreeNode;
import com.zlwang.school.modules.permission.vo.SystemPermissionResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemPermissionService {

    private static final Set<Long> BUILT_IN_PERMISSION_IDS = Set.of(
        1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L,
        101L, 102L, 103L, 104L, 105L, 201L, 202L, 203L, 204L, 205L, 206L
    );

    private final SystemPermissionRepository systemPermissionRepository;

    public SystemPermissionService(SystemPermissionRepository systemPermissionRepository) {
        this.systemPermissionRepository = systemPermissionRepository;
    }

    public List<PermissionTreeNode> findTree() {
        List<SystemPermission> permissions = sortedPermissions();
        Map<Long, List<SystemPermission>> childrenByParent = permissions.stream()
            .collect(Collectors.groupingBy(SystemPermission::parentId));
        return childrenByParent.getOrDefault(0L, List.of()).stream()
            .map(permission -> toTreeNode(permission, childrenByParent))
            .toList();
    }

    public SystemPermissionResponse findById(long id) {
        return SystemPermissionResponse.from(requirePermission(id));
    }

    public long create(CreatePermissionRequest request, long operatorId) {
        List<SystemPermission> permissions = sortedPermissions();
        validateParent(request.parentId(), request.permissionType(), null, permissions);
        validateTypeFields(request.permissionType(), request.apiMethod(), request.apiPath());
        String permissionCode = request.permissionCode().trim();
        if (systemPermissionRepository.existsByCode(permissionCode)) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限编码已存在");
        }
        try {
            return systemPermissionRepository.create(new CreateSystemPermission(
                request.parentId(),
                request.permissionName().trim(),
                permissionCode,
                request.permissionType(),
                normalize(request.routePath()),
                normalize(request.componentPath()),
                normalize(request.icon()),
                normalize(request.apiMethod()),
                normalize(request.apiPath()),
                request.sortNo(),
                request.visible(),
                request.status(),
                normalize(request.remark()),
                operatorId
            ));
        } catch (DataIntegrityViolationException | IllegalStateException exception) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限编码已存在");
        }
    }

    public void update(long id, UpdatePermissionRequest request, long operatorId) {
        SystemPermission current = requirePermission(id);
        List<SystemPermission> permissions = sortedPermissions();
        validateParent(request.parentId(), current.permissionType(), id, permissions);
        validateTypeFields(current.permissionType(), request.apiMethod(), request.apiPath());
        if (BUILT_IN_PERMISSION_IDS.contains(id) && request.status() == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能禁用内置权限");
        }
        if (!systemPermissionRepository.update(new UpdateSystemPermission(
            id,
            request.parentId(),
            request.permissionName().trim(),
            normalize(request.routePath()),
            normalize(request.componentPath()),
            normalize(request.icon()),
            normalize(request.apiMethod()),
            normalize(request.apiPath()),
            request.sortNo(),
            request.visible(),
            request.status(),
            normalize(request.remark()),
            operatorId
        ))) {
            throw notFound(id);
        }
    }

    public void delete(long id, long operatorId) {
        requirePermission(id);
        if (BUILT_IN_PERMISSION_IDS.contains(id)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能删除内置权限");
        }
        if (systemPermissionRepository.countChildren(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限存在子节点，不能删除");
        }
        if (systemPermissionRepository.countAssignedRoles(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限仍被角色使用，不能删除");
        }
        if (!systemPermissionRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private List<SystemPermission> sortedPermissions() {
        return systemPermissionRepository.findAll().stream()
            .sorted(Comparator.comparingInt(SystemPermission::sortNo).thenComparingLong(SystemPermission::id))
            .toList();
    }

    private PermissionTreeNode toTreeNode(
        SystemPermission permission,
        Map<Long, List<SystemPermission>> childrenByParent
    ) {
        List<PermissionTreeNode> children = childrenByParent.getOrDefault(permission.id(), List.of())
            .stream()
            .map(child -> toTreeNode(child, childrenByParent))
            .toList();
        return PermissionTreeNode.from(permission, children);
    }

    private void validateParent(
        long parentId,
        String permissionType,
        Long currentId,
        List<SystemPermission> permissions
    ) {
        if (parentId == 0) {
            if (!"MENU".equals(permissionType)) {
                throw validation("BUTTON 和 API 权限必须挂在菜单下");
            }
            return;
        }
        Map<Long, SystemPermission> byId = permissions.stream()
            .collect(Collectors.toMap(SystemPermission::id, Function.identity()));
        SystemPermission parent = byId.get(parentId);
        if (parent == null) {
            throw validation("父级权限不存在");
        }
        if (!"MENU".equals(parent.permissionType())) {
            throw validation("父级权限必须是菜单");
        }
        if (currentId == null) {
            return;
        }
        long cursor = parentId;
        while (cursor != 0) {
            if (cursor == currentId) {
                throw validation("不能将权限移动到自身或子节点下");
            }
            SystemPermission cursorPermission = byId.get(cursor);
            cursor = cursorPermission == null ? 0 : cursorPermission.parentId();
        }
    }

    private void validateTypeFields(String permissionType, String apiMethod, String apiPath) {
        boolean hasApiMethod = StringUtils.hasText(apiMethod);
        boolean hasApiPath = StringUtils.hasText(apiPath);
        if ("MENU".equals(permissionType) && (hasApiMethod || hasApiPath)) {
            throw validation("菜单权限不能配置接口方法或路径");
        }
        if (!"MENU".equals(permissionType) && (!hasApiMethod || !hasApiPath)) {
            throw validation("BUTTON 和 API 权限必须同时配置接口方法和路径");
        }
    }

    private SystemPermission requirePermission(long id) {
        return systemPermissionRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException validation(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "权限不存在：" + id);
    }
}
