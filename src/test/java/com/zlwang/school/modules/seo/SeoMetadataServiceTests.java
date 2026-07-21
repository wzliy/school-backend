package com.zlwang.school.modules.seo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteConfigType;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
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
class SeoMetadataServiceTests {

    @Mock
    private CmsColumnRepository cmsColumnRepository;

    @Mock
    private CmsContentRepository cmsContentRepository;

    @Mock
    private CmsSiteConfigRepository cmsSiteConfigRepository;

    private SeoMetadataService service;

    @BeforeEach
    void setUp() {
        service = new SeoMetadataService(
            cmsColumnRepository,
            cmsContentRepository,
            cmsSiteConfigRepository
        );
    }

    @Test
    void contentSeoUsesContentThenColumnThenNaturalAndSiteFallbacks() {
        CmsColumn column = column(" /news/ ", "栏目 SEO", "栏目关键词", "栏目描述");
        CmsContent content = content("内容 SEO", null, null);
        when(cmsContentRepository.findById(7L)).thenReturn(Optional.of(content));
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column));
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(defaults());

        SeoMetadata metadata = service.resolveContent(7L);

        assertThat(metadata.title()).isEqualTo("内容 SEO");
        assertThat(metadata.keywords()).isEqualTo("栏目关键词");
        assertThat(metadata.description()).isEqualTo("栏目描述");
        assertThat(metadata.canonicalPath()).isEqualTo("/news/7");
    }

    @Test
    void contentDescriptionFallsBackToSummaryBeforeSiteDefault() {
        CmsColumn column = column("/news", null, null, null);
        CmsContent content = content(null, null, null);
        when(cmsContentRepository.findById(7L)).thenReturn(Optional.of(content));
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column));
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(defaults());

        SeoMetadata metadata = service.resolveContent(7L);

        assertThat(metadata.title()).isEqualTo("校园新闻");
        assertThat(metadata.keywords()).isEqualTo("默认关键词");
        assertThat(metadata.description()).isEqualTo("新闻摘要");
    }

    @Test
    void columnSeoFallsBackToNameAndSiteDefaults() {
        CmsColumn column = column(null, null, null, null);
        when(cmsColumnRepository.findById(101L)).thenReturn(Optional.of(column));
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(defaults());

        SeoMetadata metadata = service.resolveColumn(101L);

        assertThat(metadata.title()).isEqualTo("新闻中心");
        assertThat(metadata.keywords()).isEqualTo("默认关键词");
        assertThat(metadata.description()).isEqualTo("默认描述");
        assertThat(metadata.canonicalPath()).isEqualTo("/columns/101");
    }

    @Test
    void pageSeoUsesSiteDefaultsAndNormalizesCanonicalPath() {
        when(cmsSiteConfigRepository.findAll(SiteScope.MAIN_SITE)).thenReturn(defaults());

        SeoMetadata metadata = service.resolvePage(SiteType.MAIN_SITE, " / ");

        assertThat(metadata.title()).isEqualTo("默认标题");
        assertThat(metadata.keywords()).isEqualTo("默认关键词");
        assertThat(metadata.description()).isEqualTo("默认描述");
        assertThat(metadata.canonicalPath()).isEqualTo("/");
    }

    private CmsColumn column(
        String routePath,
        String seoTitle,
        String seoKeywords,
        String seoDescription
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new CmsColumn(
            101L,
            0,
            SiteType.MAIN_SITE,
            "新闻中心",
            "news",
            ColumnType.LIST,
            routePath,
            null,
            PageTemplateKey.ARTICLE_LIST,
            PageTemplateKey.ARTICLE_DETAIL,
            Map.of(),
            null,
            10,
            true,
            true,
            seoTitle,
            seoKeywords,
            seoDescription,
            null,
            now,
            now
        );
    }

    private CmsContent content(String seoTitle, String seoKeywords, String seoDescription) {
        LocalDateTime now = LocalDateTime.now();
        return new CmsContent(
            7L,
            101L,
            "新闻中心",
            SiteType.MAIN_SITE,
            "校园新闻",
            null,
            "新闻摘要",
            "<p>正文</p>",
            null,
            null,
            null,
            now,
            ContentStatus.PUBLISHED,
            false,
            false,
            10,
            0,
            seoTitle,
            seoKeywords,
            seoDescription,
            Map.of(),
            List.of(),
            now,
            now
        );
    }

    private List<CmsSiteConfig> defaults() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
            config(1L, "defaultSeoTitle", "默认标题", now),
            config(2L, "defaultSeoKeywords", "默认关键词", now),
            config(3L, "defaultSeoDescription", "默认描述", now)
        );
    }

    private CmsSiteConfig config(long id, String key, String value, LocalDateTime now) {
        return new CmsSiteConfig(
            id,
            SiteScope.MAIN_SITE,
            key,
            value,
            SiteConfigType.STRING,
            key,
            now,
            now
        );
    }
}
