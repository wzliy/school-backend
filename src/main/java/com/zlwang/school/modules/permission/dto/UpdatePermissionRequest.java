package com.zlwang.school.modules.permission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
    @PositiveOrZero(message = "不能小于 0")
    long parentId,

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String permissionName,

    @Size(max = 255, message = "长度不能超过 255 个字符")
    String routePath,

    @Size(max = 255, message = "长度不能超过 255 个字符")
    String componentPath,

    @Size(max = 64, message = "长度不能超过 64 个字符")
    String icon,

    @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE|\\*", message = "接口方法不正确")
    String apiMethod,

    @Size(max = 255, message = "长度不能超过 255 个字符")
    String apiPath,

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "不能小于 0")
    @Max(value = 9999, message = "不能大于 9999")
    Integer sortNo,

    @NotNull(message = "不能为空")
    Boolean visible,

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "只能为 0 或 1")
    @Max(value = 1, message = "只能为 0 或 1")
    Integer status,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String remark
) {
}
