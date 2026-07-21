package com.zlwang.school.modules.portal.service;

import static com.zlwang.school.modules.page.model.PageCode.HOME;
import static com.zlwang.school.modules.page.model.PageCode.RECRUIT_HOME;
import static com.zlwang.school.modules.page.model.PageSectionType.CONTACT_INFO;
import static com.zlwang.school.modules.page.model.PageSectionType.CONTENT_FEED;
import static com.zlwang.school.modules.page.model.PageSectionType.FRIEND_LINKS;
import static com.zlwang.school.modules.page.model.PageSectionType.HERO_BANNER;
import static com.zlwang.school.modules.page.model.PageSectionType.IMAGE_GALLERY;
import static com.zlwang.school.modules.page.model.PageSectionType.QUICK_LINKS;
import static com.zlwang.school.modules.template.model.SiteType.MAIN_SITE;
import static com.zlwang.school.modules.template.model.SiteType.RECRUIT_SITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.link.repository.CmsFriendLinkRepository;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.model.PageSectionType;
import com.zlwang.school.modules.page.repository.PageSectionRepository;
import com.zlwang.school.modules.page.service.PageSectionRegistry;
import com.zlwang.school.modules.portal.vo.PortalPageResponse;
import com.zlwang.school.modules.portal.vo.PortalPageSectionResponse;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteConfigType;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortalPageServiceTests {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-20T02:00:00Z"),
        ZoneId.of("Asia/Shanghai")
    );
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 10, 0);

    @Mock
    private PageSectionRepository pageSectionRepository;
    @Mock
    private CmsColumnRepository cmsColumnRepository;
    @Mock
    private CmsContentRepository cmsContentRepository;
    @Mock
    private CmsBannerRepository cmsBannerRepository;
    @Mock
    private CmsFriendLinkRepository cmsFriendLinkRepository;
    @Mock
    private CmsSiteConfigRepository cmsSiteConfigRepository;
    @Mock
    private SeoMetadataService seoMetadataService;

    private PortalPageService service;

    @BeforeEach
    void setUp() {
        service = new PortalPageService(
            new PageSectionRegistry(),
            pageSectionRepository,
            cmsColumnRepository,
            cmsContentRepository,
            cmsBannerRepository,
            cmsFriendLinkRepository,
            cmsSiteConfigRepository,
            seoMetadataService,
            CLOCK
        );
    }

    @Test
    void aggregatesEnabledHomeSectionsAndFiltersUnavailableReferences() {
        CmsColumn news = column(101, MAIN_SITE, true, true, 20);
        CmsColumn disabled = column(102, MAIN_SITE, false, true, 30);
        CmsColumn hidden = column(103, MAIN_SITE, true, false, 40);
        CmsColumn serviceColumn = column(104, MAIN_SITE, true, true, 10);
        CmsContent published = content(1, 101, MAIN_SITE, NOW.minusHours(1), "/uploads/news.jpg");
        CmsContent future = content(900, 101, MAIN_SITE, NOW.plusHours(1), null);
        CmsContent validTarget = content(901, 101, MAIN_SITE, NOW.minusMinutes(10), null);
        when(cmsColumnRepository.findAll(MAIN_SITE))
            .thenReturn(List.of(news, disabled, hidden, serviceColumn));
        when(pageSectionRepository.findAll(MAIN_SITE, HOME)).thenReturn(List.of(
            section(1, HOME, MAIN_SITE, "HERO", HERO_BANNER, null, null, Map.of("bannerPosition", "HOME")),
            section(2, HOME, MAIN_SITE, "NEWS", CONTENT_FEED, 101L, 6, Map.of()),
            section(3, HOME, MAIN_SITE, "DISABLED_SOURCE", CONTENT_FEED, 102L, 6, Map.of()),
            section(4, HOME, MAIN_SITE, "QUICK", QUICK_LINKS, null, 1, Map.of()),
            section(5, HOME, MAIN_SITE, "GALLERY", IMAGE_GALLERY, null, 8, Map.of()),
            section(6, HOME, MAIN_SITE, "FRIENDS", FRIEND_LINKS, null, 10, Map.of()),
            disabledSection(7, HOME, MAIN_SITE)
        ));
        when(cmsBannerRepository.findActive(MAIN_SITE, BannerPosition.HOME, NOW)).thenReturn(List.of(
            banner(1, BannerLinkType.EXTERNAL, null),
            banner(2, BannerLinkType.CONTENT, 900L),
            banner(3, BannerLinkType.CONTENT, 901L),
            banner(4, BannerLinkType.COLUMN, 102L)
        ));
        when(cmsContentRepository.findById(900L)).thenReturn(Optional.of(future));
        when(cmsContentRepository.findById(901L)).thenReturn(Optional.of(validTarget));
        when(cmsContentRepository.findPublishedByColumn(101L, MAIN_SITE, NOW, 6))
            .thenReturn(List.of(published));
        when(cmsContentRepository.findPublishedGallery(MAIN_SITE, NOW, 8))
            .thenReturn(List.of(published));
        when(cmsFriendLinkRepository.findEnabledForSite(SiteScope.MAIN_SITE, 10))
            .thenReturn(List.of(friendLink()));
        when(cmsSiteConfigRepository.findAll(SiteScope.GLOBAL)).thenReturn(List.of(
            config(1, SiteScope.GLOBAL, "siteName", "高校官网"),
            config(2, SiteScope.GLOBAL, "contactPhone", "010-12345678")
        ));
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(List.of(
            config(3, SiteScope.MAIN_SITE, "siteName", "主站")
        ));
        when(seoMetadataService.resolvePage(MAIN_SITE, "/"))
            .thenReturn(new SeoMetadata("主站", null, null, "/"));

        PortalPageResponse page = service.findPage(HOME);

        assertThat(page.templateKey()).isEqualTo(PageTemplateKey.HOME);
        assertThat(page.siteConfig()).containsEntry("siteName", "主站");
        assertThat(page.sections()).extracting(PortalPageSectionResponse::sectionCode)
            .containsExactly("HERO", "NEWS", "QUICK", "GALLERY", "FRIENDS");
        assertThat(find(page, "HERO").banners()).extracting("id").containsExactly(1L, 3L);
        assertThat(find(page, "NEWS").sourceColumn().id()).isEqualTo(101L);
        assertThat(find(page, "NEWS").contents()).singleElement()
            .satisfies(item -> assertThat(item.title()).isEqualTo("内容1"));
        assertThat(find(page, "QUICK").links()).singleElement()
            .satisfies(item -> assertThat(item.id()).isEqualTo(104L));
        assertThat(find(page, "GALLERY").contents()).hasSize(1);
        assertThat(find(page, "FRIENDS").friendLinks()).hasSize(1);
    }

    @Test
    void recruitContactUsesMergedConfigAndVisibilityFlags() {
        when(cmsColumnRepository.findAll(RECRUIT_SITE)).thenReturn(List.of());
        when(pageSectionRepository.findAll(RECRUIT_SITE, RECRUIT_HOME)).thenReturn(List.of(
            section(
                1,
                RECRUIT_HOME,
                RECRUIT_SITE,
                "CONTACT",
                CONTACT_INFO,
                null,
                null,
                Map.of("showPhone", true, "showEmail", false, "showAddress", true)
            )
        ));
        when(cmsSiteConfigRepository.findAll(SiteScope.GLOBAL)).thenReturn(List.of(
            config(1, SiteScope.GLOBAL, "contactPhone", "010-12345678"),
            config(2, SiteScope.GLOBAL, "contactEmail", "office@example.edu.cn"),
            config(3, SiteScope.GLOBAL, "contactAddress", "旧地址")
        ));
        when(cmsSiteConfigRepository.findAll(SiteScope.RECRUIT_SITE)).thenReturn(List.of(
            config(4, SiteScope.RECRUIT_SITE, "contactAddress", "招生办公室")
        ));
        when(seoMetadataService.resolvePage(RECRUIT_SITE, "/recruit"))
            .thenReturn(new SeoMetadata("招生就业", null, null, "/recruit"));

        PortalPageResponse page = service.findPage(RECRUIT_HOME);

        assertThat(page.siteType()).isEqualTo(RECRUIT_SITE);
        assertThat(page.templateKey()).isEqualTo(PageTemplateKey.RECRUIT_HOME);
        assertThat(page.sections()).singleElement().satisfies(section -> {
            assertThat(section.contact()).containsEntry("phone", "010-12345678");
            assertThat(section.contact()).containsEntry("address", "招生办公室");
            assertThat(section.contact()).doesNotContainKey("email");
        });
    }

    private PortalPageSectionResponse find(PortalPageResponse page, String code) {
        return page.sections().stream()
            .filter(section -> code.equals(section.sectionCode()))
            .findFirst()
            .orElseThrow();
    }

    private PageSection section(
        long id,
        PageCode pageCode,
        SiteType siteType,
        String code,
        PageSectionType type,
        Long columnId,
        Integer displayCount,
        Map<String, Object> config
    ) {
        return new PageSection(
            id,
            siteType,
            pageCode,
            code,
            code,
            type,
            columnId,
            displayCount,
            "DEFAULT",
            config,
            (int) id * 10,
            true,
            NOW,
            NOW
        );
    }

    private PageSection disabledSection(long id, PageCode pageCode, SiteType siteType) {
        PageSection section = section(
            id,
            pageCode,
            siteType,
            "DISABLED",
            CONTACT_INFO,
            null,
            null,
            Map.of()
        );
        return new PageSection(
            section.id(),
            section.siteType(),
            section.pageCode(),
            section.sectionCode(),
            section.sectionName(),
            section.sectionType(),
            section.dataSourceColumnId(),
            section.displayCount(),
            section.displayStyle(),
            section.config(),
            section.sortNo(),
            false,
            section.createdAt(),
            section.updatedAt()
        );
    }

    private CmsColumn column(long id, SiteType siteType, boolean enabled, boolean navVisible, int sortNo) {
        return new CmsColumn(
            id,
            0,
            siteType,
            "栏目" + id,
            "column-" + id,
            ColumnType.LIST,
            "/column-" + id,
            null,
            PageTemplateKey.ARTICLE_LIST,
            PageTemplateKey.ARTICLE_DETAIL,
            Map.of(),
            null,
            sortNo,
            navVisible,
            enabled,
            null,
            null,
            null,
            null,
            NOW,
            NOW
        );
    }

    private CmsContent content(
        long id,
        long columnId,
        SiteType siteType,
        LocalDateTime publishAt,
        String coverUrl
    ) {
        return new CmsContent(
            id,
            columnId,
            "栏目" + columnId,
            siteType,
            "内容" + id,
            null,
            "摘要",
            "<p>正文</p>",
            coverUrl,
            null,
            null,
            publishAt,
            ContentStatus.PUBLISHED,
            false,
            coverUrl != null,
            10,
            0,
            null,
            null,
            null,
            Map.of(),
            List.of(),
            NOW,
            NOW
        );
    }

    private CmsBanner banner(long id, BannerLinkType linkType, Long linkRefId) {
        return new CmsBanner(
            id,
            MAIN_SITE,
            BannerPosition.HOME,
            "Banner" + id,
            null,
            "/uploads/banner-" + id + ".jpg",
            null,
            linkType,
            linkRefId,
            linkType == BannerLinkType.EXTERNAL ? "https://example.edu.cn" : null,
            BannerLinkTarget._self,
            (int) id,
            true,
            null,
            null,
            null,
            NOW,
            NOW
        );
    }

    private CmsFriendLink friendLink() {
        return new CmsFriendLink(
            1,
            SiteScope.GLOBAL,
            "教育部",
            "https://www.moe.gov.cn/",
            null,
            10,
            true,
            null,
            NOW,
            NOW
        );
    }

    private CmsSiteConfig config(long id, SiteScope scope, String key, String value) {
        return new CmsSiteConfig(
            id,
            scope,
            key,
            value,
            SiteConfigType.STRING,
            key,
            NOW,
            NOW
        );
    }
}
