package com.zlwang.school.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String username,

    @NotBlank(message = "不能为空")
    @Size(max = 128, message = "长度不能超过 128 个字符")
    String password
) {
}
