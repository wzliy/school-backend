package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.CmsBanner;

public record PortalBannerResponse(
    long id,
    String title,
    String subtitle,
    String imageUrl,
    String mobileImageUrl,
    BannerLinkType linkType,
    Long linkRefId,
    String linkUrl,
    BannerLinkTarget linkTarget
) {

    public static PortalBannerResponse from(CmsBanner banner) {
        return new PortalBannerResponse(
            banner.id(),
            banner.title(),
            banner.subtitle(),
            banner.imageUrl(),
            banner.mobileImageUrl(),
            banner.linkType(),
            banner.linkRefId(),
            banner.linkUrl(),
            banner.linkTarget()
        );
    }
}
