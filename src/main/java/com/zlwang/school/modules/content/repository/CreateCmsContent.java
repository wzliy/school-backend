package com.zlwang.school.modules.content.repository;

import com.zlwang.school.modules.content.dto.ContentAttachmentRequest;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CreateCmsContent(
    long columnId,
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
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    Map<String, Object> extensionData,
    List<ContentAttachmentRequest> attachments,
    long operatorId
) {
}
