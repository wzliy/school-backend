package com.zlwang.school.modules.column.vo;

import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ColumnTreeNode(
    long id,
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<ColumnTreeNode> children
) {

    public static ColumnTreeNode from(CmsColumn column, List<ColumnTreeNode> children) {
        return new ColumnTreeNode(
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
            column.sortNo(),
            column.navVisible(),
            column.enabled(),
            column.seoTitle(),
            column.seoKeywords(),
            column.seoDescription(),
            column.remark(),
            column.createdAt(),
            column.updatedAt(),
            List.copyOf(children)
        );
    }
}
