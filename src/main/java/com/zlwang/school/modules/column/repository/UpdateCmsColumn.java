package com.zlwang.school.modules.column.repository;

import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import java.util.Map;

public record UpdateCmsColumn(
    long id,
    long parentId,
    String columnName,
    String columnCode,
    ColumnType columnType,
    String routePath,
    String externalUrl,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    Map<String, Object> templateConfig,
    String coverUrl,
    int sortNo,
    boolean navVisible,
    boolean enabled,
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    String remark,
    long operatorId
) {
}
