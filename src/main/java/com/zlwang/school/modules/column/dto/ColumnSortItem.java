package com.zlwang.school.modules.column.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ColumnSortItem(
    @Positive long id,
    @PositiveOrZero long parentId,
    @PositiveOrZero int sortNo
) {
}
