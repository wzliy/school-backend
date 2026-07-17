package com.zlwang.school.modules.link.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SortFriendLinksRequest(
    @NotEmpty(message = "排序项不能为空")
    @Size(max = 100, message = "单次最多更新 100 个排序项")
    List<@Valid FriendLinkSortItem> items
) {
}
