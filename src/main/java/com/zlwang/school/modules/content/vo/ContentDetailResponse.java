package com.zlwang.school.modules.content.vo;

import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentAttachment;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ContentDetailResponse(
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

    public static ContentDetailResponse from(CmsContent content) {
        return new ContentDetailResponse(
            content.id(),
            content.columnId(),
            content.columnName(),
            content.siteType(),
            content.title(),
            content.subtitle(),
            content.summary(),
            content.contentHtml(),
            content.coverUrl(),
            content.source(),
            content.author(),
            content.publishAt(),
            content.status(),
            content.topFlag(),
            content.recommendFlag(),
            content.sortNo(),
            content.viewCount(),
            content.seoTitle(),
            content.seoKeywords(),
            content.seoDescription(),
            content.extensionData(),
            content.attachments(),
            content.createdAt(),
            content.updatedAt()
        );
    }
}
