package com.zlwang.school.modules.portal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.exception.BusinessException;
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
import com.zlwang.school.modules.portal.vo.PortalBannerResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnTreeNodeResponse;
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
class PortalSiteServiceTests {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-07-21T02:00:00Z"),
        ZoneId.of("Asia/Shanghai")
    );
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 21, 10, 0);

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

    private PortalSiteService service;

    @BeforeEach
    void setUp() {
        service = new PortalSiteService(
            cmsColumnRepository,
            cmsContentRepository,
            cmsBannerRepository,
            cmsFriendLinkRepository,
            cmsSiteConfigRepository,
            CLOCK
        );
    }

    @Test
    void mergesSiteConfigAndBuildsPublicNavigationTree() {
        when(cmsSiteConfigRepository.findAll(SiteScope.GLOBAL)).thenReturn(List.of(
            config(1, SiteScope.GLOBAL, "siteName", "高校官网"),
            config(2, SiteScope.GLOBAL, "contactPhone", "010-12345678")
        ));
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(List.of(
            config(3, SiteScope.MAIN_SITE, "siteName", "主站")
        ));
        when(cmsColumnRepository.findAll(SiteType.MAIN_SITE)).thenReturn(List.of(
            column(1, 0, true, true, 20),
            column(2, 1, true, true, 10),
            column(3, 0, true, false, 30),
            column(4, 0, false, true, 40),
            column(5, 3, true, true, 5)
        ));

        assertThat(service.findSiteConfig(SiteType.MAIN_SITE).configs())
            .containsEntry("siteName", "主站")
            .containsEntry("contactPhone", "010-12345678");

        List<PortalColumnTreeNodeResponse> navigation = service.findNavigation(SiteType.MAIN_SITE);
        assertThat(navigation).extracting(PortalColumnTreeNodeResponse::id)
            .containsExactly(5L, 1L);
        assertThat(navigation.get(1).children()).singleElement()
            .satisfies(child -> assertThat(child.id()).isEqualTo(2L));

        List<PortalColumnTreeNodeResponse> allColumns = service.findColumnTree(SiteType.MAIN_SITE);
        assertThat(allColumns).extracting(PortalColumnTreeNodeResponse::id)
            .containsExactly(1L, 3L);
        assertThat(allColumns.get(1).children()).singleElement()
            .satisfies(child -> assertThat(child.id()).isEqualTo(5L));
    }

    @Test
    void activeBannersFilterUnavailableInternalTargetsAndRejectSiteMismatch() {
        CmsColumn enabled = column(101, 0, true, true, 10);
        CmsColumn disabled = column(102, 0, false, true, 20);
        CmsContent future = content(900, NOW.plusHours(1));
        CmsContent published = content(901, NOW.minusMinutes(10));
        when(cmsColumnRepository.findAll(SiteType.MAIN_SITE))
            .thenReturn(List.of(enabled, disabled));
        when(cmsBannerRepository.findActive(SiteType.MAIN_SITE, BannerPosition.HOME, NOW))
            .thenReturn(List.of(
                banner(1, BannerLinkType.EXTERNAL, null),
                banner(2, BannerLinkType.CONTENT, 900L),
                banner(3, BannerLinkType.CONTENT, 901L),
                banner(4, BannerLinkType.COLUMN, 102L)
            ));
        when(cmsContentRepository.findById(900L)).thenReturn(Optional.of(future));
        when(cmsContentRepository.findById(901L)).thenReturn(Optional.of(published));

        assertThat(service.findBanners(SiteType.MAIN_SITE, BannerPosition.HOME))
            .extracting(PortalBannerResponse::id)
            .containsExactly(1L, 3L);

        assertThatThrownBy(() -> service.findBanners(
            SiteType.RECRUIT_SITE,
            BannerPosition.HOME
        )).isInstanceOf(BusinessException.class)
            .hasMessage("Banner 位置与站点不匹配");
    }

    @Test
    void friendLinksReturnPublicFieldsFromGlobalAndSiteScope() {
        when(cmsFriendLinkRepository.findEnabledForSite(SiteScope.RECRUIT_SITE, 100))
            .thenReturn(List.of(friendLink()));

        assertThat(service.findFriendLinks(SiteType.RECRUIT_SITE)).singleElement()
            .satisfies(link -> {
                assertThat(link.name()).isEqualTo("教育部");
                assertThat(link.linkUrl()).isEqualTo("https://www.moe.gov.cn/");
            });
    }

    private CmsColumn column(
        long id,
        long parentId,
        boolean enabled,
        boolean navVisible,
        int sortNo
    ) {
        return new CmsColumn(
            id,
            parentId,
            SiteType.MAIN_SITE,
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

    private CmsContent content(long id, LocalDateTime publishAt) {
        return new CmsContent(
            id,
            101,
            "新闻中心",
            SiteType.MAIN_SITE,
            "内容" + id,
            null,
            "摘要",
            "<p>正文</p>",
            null,
            null,
            null,
            publishAt,
            ContentStatus.PUBLISHED,
            false,
            false,
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
            SiteType.MAIN_SITE,
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
            "后台备注",
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
