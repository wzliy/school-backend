package com.zlwang.school.modules.column.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SortColumnsRequest(@NotEmpty List<@Valid ColumnSortItem> items) {

    public SortColumnsRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
