package com.zlwang.school.modules.template.model;

import java.util.Objects;

public record EditorFieldOption(String value, String label) {

    public EditorFieldOption {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(label, "label must not be null");
    }
}
