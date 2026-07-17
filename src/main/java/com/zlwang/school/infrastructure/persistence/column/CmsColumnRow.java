package com.zlwang.school.infrastructure.persistence.column;

import java.time.LocalDateTime;

public record CmsColumnRow(
    long id,
    long parentId,
    String siteType,
    String columnName,
    String columnCode,
    String columnType,
    String routePath,
    String externalUrl,
    String templateKey,
    String detailTemplateKey,
    String templateConfig,
    String coverUrl,
    int sortNo,
    int navVisible,
    int enabled,
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
