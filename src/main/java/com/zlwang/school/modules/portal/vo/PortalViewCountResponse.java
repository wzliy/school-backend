package com.zlwang.school.modules.portal.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "浏览量递增结果",
    example = "{\"id\":1001,\"viewCount\":121}"
)
public record PortalViewCountResponse(long id, long viewCount) {
}
