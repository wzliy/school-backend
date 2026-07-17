package com.zlwang.school.modules.link.dto;

import com.zlwang.school.modules.site.model.SiteScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateFriendLinkRequest(
    @NotNull SiteScope siteType,
    @NotBlank @Size(max = 128) String name,
    @NotBlank @Size(max = 512) String linkUrl,
    @Size(max = 512) String logoUrl,
    @PositiveOrZero int sortNo,
    @NotNull Boolean enabled,
    @Size(max = 512) String remark
) {
}
