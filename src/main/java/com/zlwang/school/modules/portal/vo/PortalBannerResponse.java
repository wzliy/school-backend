package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.CmsBanner;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "当前有效的公开 Banner",
    example = "{\"id\":1,\"title\":\"欢迎报考\",\"subtitle\":\"招生专题\",\"imageUrl\":\"/uploads/banner.jpg\",\"mobileImageUrl\":\"/uploads/banner-mobile.jpg\",\"linkType\":\"EXTERNAL\",\"linkUrl\":\"https://www.example.edu.cn\",\"linkTarget\":\"_blank\"}"
)
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
