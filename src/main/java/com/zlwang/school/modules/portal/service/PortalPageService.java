package com.zlwang.school.modules.portal.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.link.repository.CmsFriendLinkRepository;
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
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortalPageService {

    private static final int DEFAULT_QUICK_LINK_LIMIT = 30;
    private static final int DEFAULT_CONTENT_FEED_LIMIT = 10;
    private static final int DEFAULT_GALLERY_LIMIT = 8;
    private static final int DEFAULT_FRIEND_LINK_LIMIT = 50;

    private final PageSectionRegistry pageSectionRegistry;
    private final PageSectionRepository pageSectionRepository;
    private final CmsColumnRepository cmsColumnRepository;
    private final CmsContentRepository cmsContentRepository;
    private final CmsBannerRepository cmsBannerRepository;
    private final CmsFriendLinkRepository cmsFriendLinkRepository;
    private final CmsSiteConfigRepository cmsSiteConfigRepository;
    private final SeoMetadataService seoMetadataService;
    private final Clock clock;

    @Autowired
    public PortalPageService(
        PageSectionRegistry pageSectionRegistry,
        PageSectionRepository pageSectionRepository,
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        CmsBannerRepository cmsBannerRepository,
        CmsFriendLinkRepository cmsFriendLinkRepository,
        CmsSiteConfigRepository cmsSiteConfigRepository,
        SeoMetadataService seoMetadataService
    ) {
        this(
            pageSectionRegistry,
            pageSectionRepository,
            cmsColumnRepository,
            cmsContentRepository,
            cmsBannerRepository,
            cmsFriendLinkRepository,
            cmsSiteConfigRepository,
            seoMetadataService,
            Clock.systemDefaultZone()
        );
    }

    PortalPageService(
        PageSectionRegistry pageSectionRegistry,
        PageSectionRepository pageSectionRepository,
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        CmsBannerRepository cmsBannerRepository,
        CmsFriendLinkRepository cmsFriendLinkRepository,
        CmsSiteConfigRepository cmsSiteConfigRepository,
        SeoMetadataService seoMetadataService,
        Clock clock
    ) {
        this.pageSectionRegistry = pageSectionRegistry;
        this.pageSectionRepository = pageSectionRepository;
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsContentRepository = cmsContentRepository;
        this.cmsBannerRepository = cmsBannerRepository;
        this.cmsFriendLinkRepository = cmsFriendLinkRepository;
        this.cmsSiteConfigRepository = cmsSiteConfigRepository;
        this.seoMetadataService = seoMetadataService;
        this.clock = clock;
    }

    public PortalPageResponse findPage(PageCode pageCode) {
        PageDefinition page = Optional.ofNullable(pageSectionRegistry.get(pageCode))
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PARAM_VALIDATION_FAILED,
                "不支持的页面编码：" + pageCode
            ));
        LocalDateTime now = LocalDateTime.now(clock);
        Map<Long, CmsColumn> columns = cmsColumnRepository.findAll(page.siteType()).stream()
            .collect(Collectors.toMap(CmsColumn::id, Function.identity()));
        Map<String, String> siteConfig = mergedSiteConfig(page.siteType());
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
        return cmsBannerRepository.findActive(siteType, position, now).stream()
            .filter(banner -> targetAvailable(banner, siteType, columns, now))
            .map(PortalBannerResponse::from)
            .toList();
    }

    private Optional<PortalPageSectionResponse> contentFeed(
        PageSection section,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now,
        PortalPageSectionResponse.Builder response
    ) {
        CmsColumn source = columns.get(section.dataSourceColumnId());
        if (!publicColumn(source, siteType)) {
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
        return columns.values().stream()
            .filter(CmsColumn::enabled)
            .filter(CmsColumn::navVisible)
            .sorted(Comparator.comparingInt(CmsColumn::sortNo).thenComparingLong(CmsColumn::id))
            .limit(limit)
            .map(PortalColumnResponse::from)
            .toList();
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
        SiteScope scope = SiteScope.valueOf(siteType.name());
        return cmsFriendLinkRepository.findEnabledForSite(
            scope,
            limit(section.displayCount(), DEFAULT_FRIEND_LINK_LIMIT)
        ).stream().map(PortalFriendLinkResponse::from).toList();
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

    private boolean targetAvailable(
        CmsBanner banner,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        if (banner.linkType() == BannerLinkType.NONE
            || banner.linkType() == BannerLinkType.EXTERNAL) {
            return true;
        }
        if (banner.linkRefId() == null) {
            return false;
        }
        if (banner.linkType() == BannerLinkType.COLUMN) {
            return publicColumn(columns.get(banner.linkRefId()), siteType);
        }
        return cmsContentRepository.findById(banner.linkRefId())
            .filter(content -> publicContent(content, siteType, columns, now))
            .isPresent();
    }

    private boolean publicContent(
        CmsContent content,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        return content.siteType() == siteType
            && content.status() == ContentStatus.PUBLISHED
            && content.publishAt() != null
            && !content.publishAt().isAfter(now)
            && publicColumn(columns.get(content.columnId()), siteType);
    }

    private boolean publicColumn(CmsColumn column, SiteType siteType) {
        return column != null && column.siteType() == siteType && column.enabled();
    }

    private Map<String, String> mergedSiteConfig(SiteType siteType) {
        Map<String, String> values = new LinkedHashMap<>();
        appendConfig(values, cmsSiteConfigRepository.findAll(SiteScope.GLOBAL));
        appendConfig(values, cmsSiteConfigRepository.findAll(SiteScope.valueOf(siteType.name())));
        return values;
    }

    private void appendConfig(Map<String, String> values, List<CmsSiteConfig> configs) {
        configs.forEach(config -> values.put(config.configKey(), config.configValue()));
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
