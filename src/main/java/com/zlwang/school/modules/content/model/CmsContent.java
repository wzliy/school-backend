package com.zlwang.school.modules.content.model;

import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CmsContent(
    long id,
    long columnId,
    String columnName,
    SiteType siteType,
    String title,
    String subtitle,
    String summary,
    String contentHtml,
    String coverUrl,
    String source,
    String author,
    LocalDateTime publishAt,
    ContentStatus status,
    boolean topFlag,
    boolean recommendFlag,
    int sortNo,
    long viewCount,
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    Map<String, Object> extensionData,
    List<ContentAttachment> attachments,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public CmsContent {
        extensionData = extensionData == null ? Map.of() : Map.copyOf(extensionData);
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}
