package com.zlwang.school.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String realName,

    @Email(message = "格式不正确")
    @Size(max = 128, message = "长度不能超过 128 个字符")
    String email,

    @Size(max = 32, message = "长度不能超过 32 个字符")
    String phone,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String remark
) {
}
