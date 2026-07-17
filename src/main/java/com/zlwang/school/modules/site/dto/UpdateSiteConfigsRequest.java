package com.zlwang.school.modules.site.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateSiteConfigsRequest(
    @NotEmpty(message = "配置项不能为空")
    @Size(max = 50, message = "单次最多更新 50 个配置项")
    List<@Valid UpdateSiteConfigItem> items
) {
}
