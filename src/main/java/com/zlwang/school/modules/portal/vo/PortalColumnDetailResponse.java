package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(
    description = "公开栏目详情",
    example = "{\"id\":101,\"parentId\":0,\"siteType\":\"MAIN_SITE\",\"name\":\"新闻中心\",\"code\":\"news\",\"columnType\":\"LIST\",\"routePath\":\"/news\",\"templateKey\":\"ARTICLE_LIST\",\"detailTemplateKey\":\"ARTICLE_DETAIL\",\"templateConfig\":{\"page\":{\"pageSize\":10}},\"seo\":{\"title\":\"新闻中心\",\"keywords\":\"高校,新闻\",\"description\":\"校园新闻\",\"canonicalPath\":\"/news\"}}"
)
public record PortalColumnDetailResponse(
    long id,
    long parentId,
    SiteType siteType,
    String name,
    String code,
    ColumnType columnType,
    String routePath,
    String externalUrl,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    Map<String, Object> templateConfig,
    String coverUrl,
    SeoMetadata seo
) {

    public PortalColumnDetailResponse {
        templateConfig = templateConfig == null ? Map.of() : Map.copyOf(templateConfig);
    }

    public static PortalColumnDetailResponse from(CmsColumn column, SeoMetadata seo) {
        return new PortalColumnDetailResponse(
            column.id(),
            column.parentId(),
            column.siteType(),
            column.columnName(),
            column.columnCode(),
            column.columnType(),
            column.routePath(),
            column.externalUrl(),
            column.templateKey(),
            column.detailTemplateKey(),
            column.templateConfig(),
            column.coverUrl(),
            seo
        );
    }
}
