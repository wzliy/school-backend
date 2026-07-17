package com.zlwang.school.modules.template.model;

import java.util.List;
import java.util.Objects;

public record EditorFieldDefinition(
    String fieldCode,
    String fieldName,
    EditorFieldType fieldType,
    boolean required,
    Object defaultValue,
    String placeholder,
    FieldValidationRule validationRule,
    List<EditorFieldOption> options,
    int sort,
    boolean enabled,
    boolean readOnly,
    String helpText
) {

    public EditorFieldDefinition {
        Objects.requireNonNull(fieldCode, "fieldCode must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(fieldType, "fieldType must not be null");
        validationRule = validationRule == null
            ? new FieldValidationRule(null, null, null, null, null, List.of())
            : validationRule;
        options = options == null ? List.of() : List.copyOf(options);
    }
}
