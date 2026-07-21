package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import java.util.List;

public record PortalNavigationNodeResponse(
    long id,
    long parentId,
    String name,
    String code,
    ColumnType columnType,
    String routePath,
    String externalUrl,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    String coverUrl,
    List<PortalNavigationNodeResponse> children
) {

    public PortalNavigationNodeResponse {
        children = children == null ? List.of() : List.copyOf(children);
    }

    public static PortalNavigationNodeResponse from(
        CmsColumn column,
        List<PortalNavigationNodeResponse> children
    ) {
        return new PortalNavigationNodeResponse(
            column.id(),
            column.parentId(),
            column.columnName(),
            column.columnCode(),
            column.columnType(),
            column.routePath(),
            column.externalUrl(),
            column.templateKey(),
            column.detailTemplateKey(),
            column.coverUrl(),
            children
        );
    }
}
