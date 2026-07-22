package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(
    description = "主站或招生就业专题首页聚合响应",
    example = "{\"pageCode\":\"HOME\",\"templateKey\":\"HOME\",\"siteType\":\"MAIN_SITE\",\"siteConfig\":{\"siteName\":\"高校官网\"},\"seo\":{\"title\":\"高校官网\",\"canonicalPath\":\"/\"},\"sections\":[]}"
)
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
