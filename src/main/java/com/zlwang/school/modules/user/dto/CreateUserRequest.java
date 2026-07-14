package com.zlwang.school.modules.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateUserRequest(
    @NotBlank(message = "不能为空")
    @Pattern(regexp = "[A-Za-z0-9_.-]{4,64}", message = "只能包含字母、数字、点、下划线和短横线，长度为 4-64")
    String username,

    @NotBlank(message = "不能为空")
    @Size(min = 8, max = 128, message = "长度必须为 8-128 个字符")
    String password,

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String realName,

    @Email(message = "格式不正确")
    @Size(max = 128, message = "长度不能超过 128 个字符")
    String email,

    @Size(max = 32, message = "长度不能超过 32 个字符")
    String phone,

    @Min(value = 0, message = "只能为 0 或 1")
    @Max(value = 1, message = "只能为 0 或 1")
    @NotNull(message = "不能为空")
    Integer status,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String remark,

    @NotNull(message = "不能为空")
    List<@Valid @Positive(message = "必须大于 0") Long> roleIds
) {
}
