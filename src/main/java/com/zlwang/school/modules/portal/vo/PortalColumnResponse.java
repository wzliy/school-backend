package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "公开栏目摘要",
    example = "{\"id\":101,\"parentId\":0,\"name\":\"新闻中心\",\"code\":\"news\",\"columnType\":\"LIST\",\"routePath\":\"/news\",\"templateKey\":\"ARTICLE_LIST\",\"detailTemplateKey\":\"ARTICLE_DETAIL\"}"
)
public record PortalColumnResponse(
    long id,
    long parentId,
    String name,
    String code,
    ColumnType columnType,
    String routePath,
    String externalUrl,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    String coverUrl
) {

    public static PortalColumnResponse from(CmsColumn column) {
        return new PortalColumnResponse(
            column.id(),
            column.parentId(),
            column.columnName(),
            column.columnCode(),
            column.columnType(),
            column.routePath(),
            column.externalUrl(),
            column.templateKey(),
            column.detailTemplateKey(),
            column.coverUrl()
        );
    }
}
