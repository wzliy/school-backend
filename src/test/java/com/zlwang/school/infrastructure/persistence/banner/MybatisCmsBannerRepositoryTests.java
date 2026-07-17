package com.zlwang.school.infrastructure.persistence.banner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.banner.dto.BannerSortItem;
import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CreateCmsBanner;
import com.zlwang.school.modules.banner.repository.UpdateCmsBanner;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisCmsBannerRepositoryTests {

    @Mock
    private CmsBannerMapper cmsBannerMapper;

    private MybatisCmsBannerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsBannerRepository(cmsBannerMapper);
    }

    @Test
    void mapsPageRowsAndEnums() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsBannerMapper.countBanners("迎新", "MAIN_SITE", "HOME", 1)).thenReturn(1L);
        when(cmsBannerMapper.findBanners("迎新", "MAIN_SITE", "HOME", 1, 0, 10))
            .thenReturn(List.of(row(now)));

        PageResult<CmsBanner> page = repository.findPage(
            "迎新",
            SiteType.MAIN_SITE,
            BannerPosition.HOME,
            true,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).singleElement().satisfies(banner -> {
            assertThat(banner.linkType()).isEqualTo(BannerLinkType.EXTERNAL);
            assertThat(banner.linkTarget()).isEqualTo(BannerLinkTarget._blank);
            assertThat(banner.enabled()).isTrue();
        });
    }

    @Test
    void createWritesAllFieldsAndReturnsGeneratedId() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 31, 18, 0);
        CreateCmsBanner command = new CreateCmsBanner(
            SiteType.MAIN_SITE,
            BannerPosition.HOME,
            "迎新专题",
            "新学期",
            "/uploads/welcome.jpg",
            "/uploads/welcome-mobile.jpg",
            BannerLinkType.EXTERNAL,
            null,
            "https://www.example.edu.cn/welcome",
            BannerLinkTarget._blank,
            10,
            true,
            start,
            end,
            "首页第一帧",
            1L
        );
        when(cmsBannerMapper.lastInsertId()).thenReturn(9L);

        assertThat(repository.create(command)).isEqualTo(9L);

        ArgumentCaptor<CmsBannerWriteRow> rowCaptor = ArgumentCaptor.forClass(CmsBannerWriteRow.class);
        verify(cmsBannerMapper).insert(rowCaptor.capture());
        CmsBannerWriteRow value = rowCaptor.getValue();
        assertThat(value.siteType()).isEqualTo("MAIN_SITE");
        assertThat(value.position()).isEqualTo("HOME");
        assertThat(value.linkType()).isEqualTo("EXTERNAL");
        assertThat(value.linkTarget()).isEqualTo("_blank");
        assertThat(value.startTime()).isEqualTo(start);
    }

    @Test
    void updateSortAndReferenceCountDelegateToMapper() {
        UpdateCmsBanner command = new UpdateCmsBanner(
            7L,
            SiteType.RECRUIT_SITE,
            BannerPosition.RECRUIT_HOME,
            "招生专题",
            null,
            "/uploads/recruit.jpg",
            null,
            BannerLinkType.COLUMN,
            200L,
            null,
            BannerLinkTarget._self,
            20,
            false,
            null,
            null,
            null,
            2L
        );
        when(cmsBannerMapper.update(org.mockito.ArgumentMatchers.eq(7L), any())).thenReturn(1);
        when(cmsBannerMapper.countReferences("COLUMN", 200L, true)).thenReturn(2L);

        assertThat(repository.update(command)).isTrue();
        repository.updateSort(List.of(new BannerSortItem(7L, 5)), 2L);

        verify(cmsBannerMapper).updateSort(7L, 5, 2L);
        assertThat(repository.countReferences(BannerLinkType.COLUMN, 200L, true)).isEqualTo(2);
    }

    private CmsBannerRow row(LocalDateTime now) {
        return new CmsBannerRow(
            1L,
            "MAIN_SITE",
            "HOME",
            "迎新专题",
            "新学期",
            "/uploads/welcome.jpg",
            "/uploads/welcome-mobile.jpg",
            "EXTERNAL",
            null,
            "https://www.example.edu.cn/welcome",
            "_blank",
            10,
            1,
            null,
            null,
            null,
            now,
            now
        );
    }
}
