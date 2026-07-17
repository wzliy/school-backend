package com.zlwang.school.modules.link.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FriendLinkSortItem(
    @Positive long id,
    @PositiveOrZero int sortNo
) {
}
