package com.zlwang.school.infrastructure.persistence.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.model.PageSectionType;
import com.zlwang.school.modules.page.repository.SavePageSection;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MybatisPageSectionRepositoryTests {

    @Mock
    private PageSectionMapper pageSectionMapper;

    private MybatisPageSectionRepository repository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        repository = new MybatisPageSectionRepository(pageSectionMapper, objectMapper);
    }

    @Test
    void mapsEnumsAndConfiguration() {
        LocalDateTime now = LocalDateTime.now();
        when(pageSectionMapper.findAll("MAIN_SITE", "HOME")).thenReturn(List.of(new PageSectionRow(
            1L,
            "MAIN_SITE",
            "HOME",
            "HERO",
            "首页轮播",
            "HERO_BANNER",
            null,
            null,
            "FULL_WIDTH",
            "{\"bannerPosition\":\"HOME\",\"autoplay\":true}",
            10,
            1,
            now,
            now
        )));

        PageSection section = repository.findAll(SiteType.MAIN_SITE, PageCode.HOME).getFirst();

        assertThat(section.sectionType()).isEqualTo(PageSectionType.HERO_BANNER);
        assertThat(section.config()).containsEntry("bannerPosition", "HOME").containsEntry("autoplay", true);
    }

    @Test
    void replaceUpdatesExistingRowsAndInsertsMissingRows() throws Exception {
        SavePageSection hero = section("HERO", PageSectionType.HERO_BANNER, null, null, "FULL_WIDTH",
            Map.of("bannerPosition", "HOME"));
        SavePageSection news = section("SCHOOL_NEWS", PageSectionType.CONTENT_FEED, 101L, 6,
            "IMAGE_TEXT", Map.of("showSummary", true));
        when(pageSectionMapper.findAnyIdByCode("MAIN_SITE", "HOME", "HERO")).thenReturn(1L);
        when(pageSectionMapper.findAnyIdByCode("MAIN_SITE", "HOME", "SCHOOL_NEWS")).thenReturn(null);
        when(pageSectionMapper.update(eq(1L), any())).thenReturn(1);
        when(pageSectionMapper.insert(any())).thenReturn(1);

        repository.replace(SiteType.MAIN_SITE, PageCode.HOME, List.of(hero, news), 9L);

        ArgumentCaptor<PageSectionWriteRow> updateCaptor = ArgumentCaptor.forClass(PageSectionWriteRow.class);
        verify(pageSectionMapper).update(eq(1L), updateCaptor.capture());
        assertThat(updateCaptor.getValue().sectionCode()).isEqualTo("HERO");
        assertThat(objectMapper.readTree(updateCaptor.getValue().configJson()).path("bannerPosition").stringValue())
            .isEqualTo("HOME");

        ArgumentCaptor<PageSectionWriteRow> insertCaptor = ArgumentCaptor.forClass(PageSectionWriteRow.class);
        verify(pageSectionMapper).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().sectionCode()).isEqualTo("SCHOOL_NEWS");
        assertThat(insertCaptor.getValue().dataSourceColumnId()).isEqualTo(101L);
        assertThat(insertCaptor.getValue().operatorId()).isEqualTo(9L);
    }

    private SavePageSection section(
        String code,
        PageSectionType type,
        Long columnId,
        Integer count,
        String style,
        Map<String, Object> config
    ) {
        return new SavePageSection(
            SiteType.MAIN_SITE,
            PageCode.HOME,
            code,
            code,
            type,
            columnId,
            count,
            style,
            config,
            10,
            true
        );
    }
}
