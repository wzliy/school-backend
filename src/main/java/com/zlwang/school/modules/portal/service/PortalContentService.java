package com.zlwang.school.modules.portal.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.portal.dto.PortalContentPageQuery;
import com.zlwang.school.modules.portal.vo.PortalColumnDetailResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnTreeNodeResponse;
import com.zlwang.school.modules.portal.vo.PortalContentDetailResponse;
import com.zlwang.school.modules.portal.vo.PortalContentSummaryResponse;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortalContentService {

    private final CmsColumnRepository cmsColumnRepository;
    private final CmsContentRepository cmsContentRepository;
    private final SeoMetadataService seoMetadataService;
    private final PortalSiteService portalSiteService;

    public PortalContentService(
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        SeoMetadataService seoMetadataService,
        PortalSiteService portalSiteService
    ) {
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsContentRepository = cmsContentRepository;
        this.seoMetadataService = seoMetadataService;
        this.portalSiteService = portalSiteService;
    }

    public List<PortalColumnTreeNodeResponse> findColumnTree(SiteType siteType) {
        return portalSiteService.findColumnTree(siteType);
    }

    public PortalColumnDetailResponse findColumn(long id) {
        CmsColumn column = requiredPublicColumn(id);
        return PortalColumnDetailResponse.from(column, seoMetadataService.resolveColumn(id));
    }

    public PageResult<PortalContentSummaryResponse> findContents(
        long columnId,
        PortalContentPageQuery query
    ) {
        CmsColumn column = requiredPublicColumn(columnId);
        PageResult<CmsContent> page = cmsContentRepository.findPublishedPage(
            column.id(),
            column.siteType(),
            portalSiteService.currentTime(),
            query.getPageNo(),
            query.getPageSize()
        );
        return PageResult.of(
            page.records().stream().map(PortalContentSummaryResponse::from).toList(),
            page.total(),
            page.pageNo(),
            page.pageSize()
        );
    }

    public PortalContentDetailResponse findContent(long id) {
        LocalDateTime now = portalSiteService.currentTime();
        CmsContent content = cmsContentRepository.findById(id)
            .filter(value -> publicContent(value, now))
            .orElseThrow(() -> contentNotFound(id));
        CmsColumn column = cmsColumnRepository.findById(content.columnId())
            .filter(value -> portalSiteService.publicColumn(value, content.siteType()))
            .orElseThrow(() -> contentNotFound(id));
        if (column.siteType() != content.siteType()) {
            throw contentNotFound(id);
        }
        return PortalContentDetailResponse.from(
            content,
            seoMetadataService.resolveContent(id)
        );
    }

    private CmsColumn requiredPublicColumn(long id) {
        return cmsColumnRepository.findById(id)
            .filter(CmsColumn::enabled)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND,
                "栏目不存在或不可访问：" + id
            ));
    }

    private boolean publicContent(CmsContent content, LocalDateTime now) {
        return content.status() == ContentStatus.PUBLISHED
            && content.publishAt() != null
            && !content.publishAt().isAfter(now);
    }

    private BusinessException contentNotFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "内容不存在或不可访问：" + id);
    }
}
