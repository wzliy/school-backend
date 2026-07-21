package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.link.model.CmsFriendLink;

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
