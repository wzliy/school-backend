package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;

public record PortalSearchResponse(
    String keyword,
    SiteType siteType,
    Long columnId,
    SeoMetadata seo,
    List<PortalContentSummaryResponse> records,
    long total,
    long pageNo,
    long pageSize
) {

    public PortalSearchResponse {
        records = records == null ? List.of() : List.copyOf(records);
    }

    public static PortalSearchResponse from(
        String keyword,
        SiteType siteType,
        Long columnId,
        SeoMetadata seo,
        PageResult<PortalContentSummaryResponse> page
    ) {
        return new PortalSearchResponse(
            keyword,
            siteType,
            columnId,
            seo,
            page.records(),
            page.total(),
            page.pageNo(),
            page.pageSize()
        );
    }
}
