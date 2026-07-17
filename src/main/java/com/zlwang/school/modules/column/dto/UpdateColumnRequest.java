package com.zlwang.school.modules.column.dto;

import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UpdateColumnRequest(
    @PositiveOrZero long parentId,
    @NotBlank @Size(max = 128) String columnName,
    @NotBlank @Size(max = 128)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "只能包含小写字母、数字和中划线")
    String columnCode,
    @NotNull ColumnType columnType,
    @Size(max = 255) String routePath,
    @Size(max = 512) String externalUrl,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    Map<String, Object> templateConfig,
    @Size(max = 512) String coverUrl,
    @PositiveOrZero int sortNo,
    @NotNull Boolean navVisible,
    @NotNull Boolean enabled,
    @Size(max = 255) String seoTitle,
    @Size(max = 512) String seoKeywords,
    @Size(max = 1024) String seoDescription,
    @Size(max = 512) String remark
) {
}
