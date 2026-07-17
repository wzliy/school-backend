package com.zlwang.school.modules.template.model;

import java.util.List;

public record EditorSchema(
    List<EditorFieldDefinition> columnFields,
    List<EditorFieldDefinition> contentFields,
    List<EditorFieldDefinition> extensionFields,
    List<EditorFieldDefinition> pageFields
) {

    public EditorSchema {
        columnFields = immutable(columnFields);
        contentFields = immutable(contentFields);
        extensionFields = immutable(extensionFields);
        pageFields = immutable(pageFields);
    }

    public static EditorSchema of(
        List<EditorFieldDefinition> columnFields,
        List<EditorFieldDefinition> contentFields,
        List<EditorFieldDefinition> extensionFields,
        List<EditorFieldDefinition> pageFields
    ) {
        return new EditorSchema(columnFields, contentFields, extensionFields, pageFields);
    }

    private static List<EditorFieldDefinition> immutable(List<EditorFieldDefinition> fields) {
        return fields == null ? List.of() : List.copyOf(fields);
    }
}
