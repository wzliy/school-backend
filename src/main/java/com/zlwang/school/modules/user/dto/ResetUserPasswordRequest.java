package com.zlwang.school.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(
    @NotBlank(message = "不能为空")
    @Size(min = 8, max = 128, message = "长度必须为 8-128 个字符")
    String password
) {
}
