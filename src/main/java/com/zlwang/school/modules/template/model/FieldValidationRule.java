package com.zlwang.school.modules.template.model;

import java.util.List;

public record FieldValidationRule(
    Integer minLength,
    Integer maxLength,
    Long minValue,
    Long maxValue,
    String pattern,
    List<String> allowedValues
) {

    public FieldValidationRule {
        allowedValues = allowedValues == null ? List.of() : List.copyOf(allowedValues);
    }

    public static FieldValidationRule text(int maxLength) {
        return new FieldValidationRule(null, maxLength, null, null, null, List.of());
    }

    public static FieldValidationRule number(long minValue, long maxValue) {
        return new FieldValidationRule(null, null, minValue, maxValue, null, List.of());
    }

    public static FieldValidationRule choices(String... values) {
        return new FieldValidationRule(null, null, null, null, null, List.of(values));
    }
}
