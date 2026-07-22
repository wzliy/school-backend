package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.link.model.CmsFriendLink;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "公开友情链接",
    example = "{\"id\":1,\"name\":\"教育部\",\"linkUrl\":\"https://www.moe.gov.cn/\",\"logoUrl\":null}"
)
public record PortalFriendLinkResponse(
    long id,
    String name,
    String linkUrl,
    String logoUrl
) {

    public static PortalFriendLinkResponse from(CmsFriendLink link) {
        return new PortalFriendLinkResponse(
            link.id(),
            link.name(),
            link.linkUrl(),
            link.logoUrl()
        );
    }
}
