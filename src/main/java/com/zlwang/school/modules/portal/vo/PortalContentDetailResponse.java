package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(
    description = "公开内容详情及附件",
    example = "{\"id\":1001,\"columnId\":101,\"columnName\":\"新闻中心\",\"siteType\":\"MAIN_SITE\",\"title\":\"校园新闻\",\"summary\":\"新闻摘要\",\"contentHtml\":\"<p>正文</p>\",\"publishAt\":\"2026-07-22T09:00:00\",\"viewCount\":120,\"extensionData\":{},\"attachments\":[],\"seo\":{\"title\":\"校园新闻\",\"canonicalPath\":\"/news/1001\"}}"
)
public record PortalContentDetailResponse(
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
    long viewCount,
    Map<String, Object> extensionData,
    List<PortalContentAttachmentResponse> attachments,
    SeoMetadata seo
) {

    public PortalContentDetailResponse {
        extensionData = extensionData == null ? Map.of() : Map.copyOf(extensionData);
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public static PortalContentDetailResponse from(CmsContent content, SeoMetadata seo) {
        return new PortalContentDetailResponse(
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
            content.viewCount(),
            content.extensionData(),
            content.attachments().stream()
                .map(PortalContentAttachmentResponse::from)
                .toList(),
            seo
        );
    }
}
