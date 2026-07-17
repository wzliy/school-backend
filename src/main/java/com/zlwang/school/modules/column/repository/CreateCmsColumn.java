package com.zlwang.school.modules.column.repository;

import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.Map;

public record CreateCmsColumn(
    long parentId,
    SiteType siteType,
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
