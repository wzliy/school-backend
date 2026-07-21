package com.zlwang.school.modules.portal.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageDefinition;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.repository.PageSectionRepository;
import com.zlwang.school.modules.page.service.PageSectionRegistry;
import com.zlwang.school.modules.portal.vo.PortalBannerResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnResponse;
import com.zlwang.school.modules.portal.vo.PortalContentSummaryResponse;
import com.zlwang.school.modules.portal.vo.PortalFriendLinkResponse;
import com.zlwang.school.modules.portal.vo.PortalPageResponse;
import com.zlwang.school.modules.portal.vo.PortalPageSectionResponse;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PortalPageService {

    private static final int DEFAULT_QUICK_LINK_LIMIT = 30;
    private static final int DEFAULT_CONTENT_FEED_LIMIT = 10;
    private static final int DEFAULT_GALLERY_LIMIT = 8;
    private static final int DEFAULT_FRIEND_LINK_LIMIT = 50;

    private final PageSectionRegistry pageSectionRegistry;
    private final PageSectionRepository pageSectionRepository;
    private final CmsContentRepository cmsContentRepository;
    private final SeoMetadataService seoMetadataService;
    private final PortalSiteService portalSiteService;

    public PortalPageService(
        PageSectionRegistry pageSectionRegistry,
        PageSectionRepository pageSectionRepository,
        CmsContentRepository cmsContentRepository,
        SeoMetadataService seoMetadataService,
        PortalSiteService portalSiteService
    ) {
        this.pageSectionRegistry = pageSectionRegistry;
        this.pageSectionRepository = pageSectionRepository;
        this.cmsContentRepository = cmsContentRepository;
        this.seoMetadataService = seoMetadataService;
        this.portalSiteService = portalSiteService;
    }

    public PortalPageResponse findPage(PageCode pageCode) {
        PageDefinition page = Optional.ofNullable(pageSectionRegistry.get(pageCode))
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PARAM_VALIDATION_FAILED,
                "不支持的页面编码：" + pageCode
            ));
        LocalDateTime now = portalSiteService.currentTime();
        Map<Long, CmsColumn> columns = portalSiteService.columns(page.siteType());
        Map<String, String> siteConfig = portalSiteService.mergedSiteConfig(page.siteType());
        List<PortalPageSectionResponse> sections = pageSectionRepository
            .findAll(page.siteType(), page.pageCode())
            .stream()
            .filter(PageSection::enabled)
            .sorted(Comparator.comparingInt(PageSection::sortNo).thenComparingLong(PageSection::id))
            .map(section -> aggregate(section, page.siteType(), columns, siteConfig, now))
            .flatMap(Optional::stream)
            .toList();
        return new PortalPageResponse(
            page.pageCode(),
            PageTemplateKey.valueOf(page.pageCode().name()),
            page.siteType(),
            siteConfig,
            seoMetadataService.resolvePage(page.siteType(), canonicalPath(page.pageCode())),
            sections
        );
    }

    private Optional<PortalPageSectionResponse> aggregate(
        PageSection section,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        Map<String, String> siteConfig,
        LocalDateTime now
    ) {
        PortalPageSectionResponse.Builder response = PortalPageSectionResponse.from(section);
        return switch (section.sectionType()) {
            case HERO_BANNER -> Optional.of(response
                .banners(activeBanners(section, siteType, columns, now))
                .build());
            case CONTENT_FEED -> contentFeed(section, siteType, columns, now, response);
            case QUICK_LINKS -> Optional.of(response
                .links(quickLinks(section, columns))
                .build());
            case IMAGE_GALLERY -> Optional.of(response
                .contents(gallery(section, siteType, now))
                .build());
            case FRIEND_LINKS -> Optional.of(response
                .friendLinks(friendLinks(section, siteType))
                .build());
            case CONTACT_INFO -> Optional.of(response
                .contact(contact(section, siteConfig))
                .build());
        };
    }

    private List<PortalBannerResponse> activeBanners(
        PageSection section,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        BannerPosition position = BannerPosition.valueOf(section.pageCode().name());
        return portalSiteService.findBanners(siteType, position, columns, now);
    }

    private Optional<PortalPageSectionResponse> contentFeed(
        PageSection section,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now,
        PortalPageSectionResponse.Builder response
    ) {
        CmsColumn source = columns.get(section.dataSourceColumnId());
        if (!portalSiteService.publicColumn(source, siteType)) {
            return Optional.empty();
        }
        List<PortalContentSummaryResponse> contents = cmsContentRepository
            .findPublishedByColumn(
                source.id(),
                siteType,
                now,
                limit(section.displayCount(), DEFAULT_CONTENT_FEED_LIMIT)
            )
            .stream()
            .map(PortalContentSummaryResponse::from)
            .toList();
        return Optional.of(response
            .sourceColumn(PortalColumnResponse.from(source))
            .contents(contents)
            .build());
    }

    private List<PortalColumnResponse> quickLinks(
        PageSection section,
        Map<Long, CmsColumn> columns
    ) {
        int limit = limit(section.displayCount(), DEFAULT_QUICK_LINK_LIMIT);
        return portalSiteService.quickLinks(columns, limit);
    }

    private List<PortalContentSummaryResponse> gallery(
        PageSection section,
        SiteType siteType,
        LocalDateTime now
    ) {
        return cmsContentRepository.findPublishedGallery(
            siteType,
            now,
            limit(section.displayCount(), DEFAULT_GALLERY_LIMIT)
        ).stream().map(PortalContentSummaryResponse::from).toList();
    }

    private List<PortalFriendLinkResponse> friendLinks(
        PageSection section,
        SiteType siteType
    ) {
        return portalSiteService.findFriendLinks(
            siteType,
            limit(section.displayCount(), DEFAULT_FRIEND_LINK_LIMIT)
        );
    }

    private Map<String, String> contact(
        PageSection section,
        Map<String, String> siteConfig
    ) {
        Map<String, String> contact = new LinkedHashMap<>();
        if (visible(section, "showPhone")) {
            contact.put("phone", siteConfig.getOrDefault("contactPhone", ""));
        }
        if (visible(section, "showEmail")) {
            contact.put("email", siteConfig.getOrDefault("contactEmail", ""));
        }
        if (visible(section, "showAddress")) {
            contact.put("address", siteConfig.getOrDefault("contactAddress", ""));
        }
        return contact;
    }

    private boolean visible(PageSection section, String key) {
        Object value = section.config().get(key);
        return !(value instanceof Boolean visible) || visible;
    }

    private int limit(Integer configured, int fallback) {
        return configured == null ? fallback : configured;
    }

    private String canonicalPath(PageCode pageCode) {
        return pageCode == PageCode.HOME ? "/" : "/recruit";
    }
}
