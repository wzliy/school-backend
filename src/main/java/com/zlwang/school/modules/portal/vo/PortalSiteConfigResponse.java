package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(
    description = "公开站点配置",
    example = "{\"siteType\":\"MAIN_SITE\",\"configs\":{\"siteName\":\"高校官网\",\"defaultSeoTitle\":\"高校官网\"}}"
)
public record PortalSiteConfigResponse(
    SiteType siteType,
    Map<String, String> configs
) {

    public PortalSiteConfigResponse {
        configs = configs == null
            ? Map.of()
            : Collections.unmodifiableMap(new LinkedHashMap<>(configs));
    }
}
