package com.zlwang.school.modules.role.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record AssignRolePermissionsRequest(
    @NotNull(message = "不能为空")
    List<@Valid @Positive(message = "必须大于 0") Long> permissionIds
) {
}
