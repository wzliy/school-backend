package com.zlwang.school.modules.template.model;

import java.util.List;
import java.util.Objects;

public record PageTemplateDefinition(
    PageTemplateKey templateKey,
    String templateName,
    String description,
    TemplateUsage usage,
    List<SiteType> compatibleSiteTypes,
    List<ColumnType> compatibleColumnTypes,
    PageTemplateKey defaultDetailTemplateKey,
    EditorSchema editorSchema
) {

    public PageTemplateDefinition {
        Objects.requireNonNull(templateKey, "templateKey must not be null");
        Objects.requireNonNull(templateName, "templateName must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        compatibleSiteTypes = List.copyOf(compatibleSiteTypes);
        compatibleColumnTypes = List.copyOf(compatibleColumnTypes);
        Objects.requireNonNull(editorSchema, "editorSchema must not be null");
    }

    public boolean supports(SiteType siteType, ColumnType columnType) {
        return compatibleSiteTypes.contains(siteType) && compatibleColumnTypes.contains(columnType);
    }
}
