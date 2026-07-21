package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.template.model.SiteType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
