package com.zlwang.school.infrastructure.persistence.column;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CreateCmsColumn;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MybatisCmsColumnRepositoryTests {

    @Mock
    private CmsColumnMapper cmsColumnMapper;

    private MybatisCmsColumnRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsColumnRepository(cmsColumnMapper, new ObjectMapper());
    }

    @Test
    void mapsTemplateConfigurationAndEnums() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsColumnMapper.findAll("MAIN_SITE")).thenReturn(List.of(new CmsColumnRow(
            101L,
            0L,
            "MAIN_SITE",
            "新闻中心",
            "news",
            "LIST",
            "/news",
            null,
            "ARTICLE_LIST",
            "ARTICLE_DETAIL",
            "{\"page\":{\"pageSize\":10},\"detail\":{}}",
            null,
            20,
            1,
            1,
            "新闻中心",
            null,
            null,
            null,
            now,
            now
        )));

        CmsColumn column = repository.findAll(SiteType.MAIN_SITE).getFirst();

        assertThat(column.templateKey()).isEqualTo(PageTemplateKey.ARTICLE_LIST);
        assertThat(column.detailTemplateKey()).isEqualTo(PageTemplateKey.ARTICLE_DETAIL);
        assertThat(column.templateConfig()).extractingByKey("page").isInstanceOf(Map.class);
    }

    @Test
    void createSerializesConfigurationAndReturnsDatabaseId() {
        CreateCmsColumn command = new CreateCmsColumn(
            0L,
            SiteType.MAIN_SITE,
            "测试栏目",
            "test-column",
            ColumnType.PAGE,
            "/test-column",
            null,
            PageTemplateKey.SINGLE_PAGE,
            null,
            Map.of("page", Map.of()),
            null,
            90,
            true,
            true,
            null,
            null,
            null,
            null,
            1L
        );
        when(cmsColumnMapper.findIdByCode("MAIN_SITE", "test-column")).thenReturn(301L);

        assertThat(repository.create(command)).isEqualTo(301L);

        verify(cmsColumnMapper).insert(
            0L,
            "MAIN_SITE",
            "测试栏目",
            "test-column",
            "PAGE",
            "/test-column",
            null,
            "SINGLE_PAGE",
            null,
            "{\"page\":{}}",
            null,
            90,
            1,
            1,
            null,
            null,
            null,
            null,
            1L
        );
    }

    @Test
    void sortUpdatesEachColumnWithinOneRepositoryOperation() {
        List<ColumnSortItem> items = List.of(
            new ColumnSortItem(301L, 0L, 10),
            new ColumnSortItem(302L, 301L, 20)
        );

        repository.updateSort(items, 1L);

        verify(cmsColumnMapper).updateSort(301L, 0L, 10, 1L);
        verify(cmsColumnMapper).updateSort(302L, 301L, 20, 1L);
    }
}
