package com.zlwang.school.infrastructure.persistence.content;

import java.time.LocalDateTime;

public record CmsContentRow(
    long id,
    long columnId,
    String columnName,
    String siteType,
    String title,
    String subtitle,
    String summary,
    String contentHtml,
    String coverUrl,
    String source,
    String author,
    LocalDateTime publishAt,
    String status,
    int topFlag,
    int recommendFlag,
    int sortNo,
    long viewCount,
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    String extensionData,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
