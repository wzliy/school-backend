package com.zlwang.school.modules.column.vo;

import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Map;

public record ColumnEditorSchemaResponse(
    long columnId,
    SiteType siteType,
    ColumnType columnType,
    PageTemplateKey templateKey,
    PageTemplateKey detailTemplateKey,
    Map<String, Object> templateConfig,
    List<EditorFieldDefinition> pageConfigFields,
    List<EditorFieldDefinition> detailConfigFields,
    List<EditorFieldDefinition> contentFields,
    List<EditorFieldDefinition> extensionFields
) {
}
