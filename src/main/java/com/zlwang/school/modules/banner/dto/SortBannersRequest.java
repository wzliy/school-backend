package com.zlwang.school.modules.banner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SortBannersRequest(@NotEmpty List<@Valid BannerSortItem> items) {

    public SortBannersRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
