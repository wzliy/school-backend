package com.zlwang.school.modules.banner.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BannerSortItem(
    @Positive long id,
    @PositiveOrZero int sortNo
) {
}
