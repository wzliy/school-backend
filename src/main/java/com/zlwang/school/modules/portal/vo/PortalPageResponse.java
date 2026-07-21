package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Map;

public record PortalPageResponse(
    PageCode pageCode,
    PageTemplateKey templateKey,
    SiteType siteType,
    Map<String, String> siteConfig,
    SeoMetadata seo,
    List<PortalPageSectionResponse> sections
) {

    public PortalPageResponse {
        siteConfig = siteConfig == null ? Map.of() : Map.copyOf(siteConfig);
        sections = sections == null ? List.of() : List.copyOf(sections);
    }
}
