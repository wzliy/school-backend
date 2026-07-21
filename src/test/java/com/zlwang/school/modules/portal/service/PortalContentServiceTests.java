package com.zlwang.school.modules.portal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.AttachmentFileType;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentAttachment;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.portal.dto.PortalContentPageQuery;
import com.zlwang.school.modules.portal.vo.PortalContentAttachmentResponse;
import com.zlwang.school.modules.portal.vo.PortalContentSummaryResponse;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortalContentServiceTests {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 21, 10, 0);

    @Mock
    private CmsColumnRepository cmsColumnRepository;
    @Mock
    private CmsContentRepository cmsContentRepository;
    @Mock
    private SeoMetadataService seoMetadataService;
    @Mock
    private PortalSiteService portalSiteService;

    private PortalContentService service;

    @BeforeEach
    void setUp() {
        service = new PortalContentService(
            cmsColumnRepository,
            cmsContentRepository,
            seoMetadataService,
            portalSiteService
        );
    }

    @Test
    void returnsEnabledColumnDetailWithResolvedSeo() {
        CmsColumn column = column(true);
        SeoMetadata seo = new SeoMetadata("新闻中心", "新闻", "栏目描述", "/news");
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column));
        when(seoMetadataService.resolveColumn(101L)).thenReturn(seo);

        assertThat(service.findColumn(101L)).satisfies(response -> {
            assertThat(response.siteType()).isEqualTo(SiteType.MAIN_SITE);
            assertThat(response.templateConfig()).containsKey("list");
            assertThat(response.seo()).isEqualTo(seo);
        });
    }

    @Test
    void publishedPageUsesRepositoryLevelVisibilityAndPagination() {
        PortalContentPageQuery query = new PortalContentPageQuery();
        query.setPageNo(2);
        query.setPageSize(5);
        CmsContent content = content(ContentStatus.PUBLISHED, NOW.minusMinutes(1));
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column(true)));
        when(portalSiteService.currentTime()).thenReturn(NOW);
        when(cmsContentRepository.findPublishedPage(
            101L,
            SiteType.MAIN_SITE,
            NOW,
            2,
            5
        )).thenReturn(PageResult.of(List.of(content), 6, 2, 5));

        PageResult<PortalContentSummaryResponse> page = service.findContents(101L, query);

        assertThat(page.total()).isEqualTo(6);
        assertThat(page.pageNo()).isEqualTo(2);
        assertThat(page.records()).singleElement()
            .satisfies(item -> assertThat(item.id()).isEqualTo(900L));
    }

    @Test
    void contentDetailReturnsPublicAttachmentFieldsAndSeo() {
        CmsContent content = content(ContentStatus.PUBLISHED, NOW.minusMinutes(1));
        SeoMetadata seo = new SeoMetadata("校园新闻", "校园", "摘要", "/news/900");
        when(portalSiteService.currentTime()).thenReturn(NOW);
        when(cmsContentRepository.findById(900L)).thenReturn(Optional.of(content));
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column(true)));
        when(portalSiteService.publicColumn(column(true), SiteType.MAIN_SITE)).thenReturn(true);
        when(seoMetadataService.resolveContent(900L)).thenReturn(seo);

        assertThat(service.findContent(900L)).satisfies(response -> {
            assertThat(response.contentHtml()).isEqualTo("<p>正文</p>");
            assertThat(response.seo()).isEqualTo(seo);
            assertThat(response.attachments())
                .extracting(PortalContentAttachmentResponse::fileName)
                .containsExactly("招生简章.pdf");
        });
    }

    @Test
    void rejectsDisabledColumnsAndNonEffectiveContentAsNotFound() {
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column(false)));
        assertThatThrownBy(() -> service.findColumn(101L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("栏目不存在或不可访问：101");

        when(portalSiteService.currentTime()).thenReturn(NOW);
        when(cmsContentRepository.findById(900L))
            .thenReturn(Optional.of(content(ContentStatus.PUBLISHED, NOW.plusMinutes(1))));
        assertThatThrownBy(() -> service.findContent(900L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("内容不存在或不可访问：900");
    }

    private CmsColumn column(boolean enabled) {
        return new CmsColumn(
            101,
            0,
            SiteType.MAIN_SITE,
            "新闻中心",
            "news",
            ColumnType.LIST,
            "/news",
            null,
            PageTemplateKey.ARTICLE_LIST,
            PageTemplateKey.ARTICLE_DETAIL,
            Map.of("list", Map.of()),
            "/uploads/news.jpg",
            10,
            true,
            enabled,
            null,
            null,
            null,
            null,
            NOW,
            NOW
        );
    }

    private CmsContent content(ContentStatus status, LocalDateTime publishAt) {
        return new CmsContent(
            900,
            101,
            "新闻中心",
            SiteType.MAIN_SITE,
            "校园新闻",
            null,
            "摘要",
            "<p>正文</p>",
            "/uploads/news.jpg",
            "学校办公室",
            "编辑部",
            publishAt,
            status,
            true,
            false,
            10,
            20,
            null,
            null,
            null,
            Map.of("gallery", List.of("/uploads/news.jpg")),
            List.of(new ContentAttachment(
                901,
                900,
                1001L,
                "招生简章.pdf",
                "/uploads/brochure.pdf",
                1024,
                AttachmentFileType.DOCUMENT,
                10,
                NOW,
                NOW
            )),
            NOW,
            NOW
        );
    }
}
