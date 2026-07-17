package com.zlwang.school.modules.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSiteConfigItem(
    @NotBlank(message = "配置键不能为空")
    @Size(max = 128, message = "配置键长度不能超过 128 个字符")
    String configKey,

    @NotNull(message = "配置值不能为空")
    @Size(max = 20_000, message = "配置值长度不能超过 20000 个字符")
    String configValue
) {
}
