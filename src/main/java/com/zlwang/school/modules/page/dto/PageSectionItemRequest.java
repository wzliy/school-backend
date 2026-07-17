package com.zlwang.school.modules.page.dto;

import com.zlwang.school.modules.page.model.PageSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record PageSectionItemRequest(
    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String sectionCode,

    @NotBlank(message = "不能为空")
    @Size(max = 128, message = "长度不能超过 128 个字符")
    String sectionName,

    @NotNull(message = "不能为空")
    PageSectionType sectionType,

    @Positive(message = "必须大于 0")
    Long dataSourceColumnId,

    @Positive(message = "必须大于 0")
    Integer displayCount,

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过 64 个字符")
    String displayStyle,

    Map<String, Object> config,

    @PositiveOrZero(message = "不能小于 0")
    @Max(value = 999999, message = "不能大于 999999")
    int sortNo,

    @NotNull(message = "不能为空")
    Boolean enabled
) {

    public PageSectionItemRequest {
        config = config == null
            ? Map.of()
            : Collections.unmodifiableMap(new LinkedHashMap<>(config));
    }
}
