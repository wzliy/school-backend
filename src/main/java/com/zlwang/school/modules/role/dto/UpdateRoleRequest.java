package com.zlwang.school.modules.role.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String roleName,

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "只能为 0 或 1")
    @Max(value = 1, message = "只能为 0 或 1")
    Integer status,

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "不能小于 0")
    @Max(value = 9999, message = "不能大于 9999")
    Integer sortNo,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String remark
) {
}
