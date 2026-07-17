package com.zlwang.school.modules.banner.dto;

import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.template.model.SiteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateBannerRequest(
    @NotNull SiteType siteType,
    @NotNull BannerPosition position,
    @NotBlank @Size(max = 255) String title,
    @Size(max = 255) String subtitle,
    @NotBlank @Size(max = 512) String imageUrl,
    @Size(max = 512) String mobileImageUrl,
    @NotNull BannerLinkType linkType,
    @Positive Long linkRefId,
    @Size(max = 512) String linkUrl,
    @NotNull BannerLinkTarget linkTarget,
    @PositiveOrZero int sortNo,
    @NotNull Boolean enabled,
    LocalDateTime startTime,
    LocalDateTime endTime,
    @Size(max = 512) String remark
) {
}
