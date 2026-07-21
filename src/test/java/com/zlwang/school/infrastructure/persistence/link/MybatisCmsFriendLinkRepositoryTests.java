package com.zlwang.school.infrastructure.persistence.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.link.dto.FriendLinkSortItem;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.link.repository.CreateCmsFriendLink;
import com.zlwang.school.modules.link.repository.UpdateCmsFriendLink;
import com.zlwang.school.modules.site.model.SiteScope;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisCmsFriendLinkRepositoryTests {

    @Mock
    private CmsFriendLinkMapper cmsFriendLinkMapper;

    private MybatisCmsFriendLinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsFriendLinkRepository(cmsFriendLinkMapper);
    }

    @Test
    void mapsPageRowsAndScope() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsFriendLinkMapper.countFriendLinks("教育", "GLOBAL", 1)).thenReturn(1L);
        when(cmsFriendLinkMapper.findFriendLinks("教育", "GLOBAL", 1, 0, 10))
            .thenReturn(List.of(row(now)));

        PageResult<CmsFriendLink> page = repository.findPage(
            "教育",
            SiteScope.GLOBAL,
            true,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).singleElement().satisfies(link -> {
            assertThat(link.siteType()).isEqualTo(SiteScope.GLOBAL);
            assertThat(link.enabled()).isTrue();
            assertThat(link.name()).isEqualTo("教育部");
        });
    }

    @Test
    void enabledSiteQueryIncludesMappedPublicRows() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsFriendLinkMapper.findEnabledForSite("MAIN_SITE", 20))
            .thenReturn(List.of(row(now)));

        assertThat(repository.findEnabledForSite(SiteScope.MAIN_SITE, 20))
            .singleElement()
            .satisfies(link -> assertThat(link.name()).isEqualTo("教育部"));
    }

    @Test
    void createsAndUpdatesAllFields() {
        CreateCmsFriendLink create = new CreateCmsFriendLink(
            SiteScope.MAIN_SITE,
            "省教育厅",
            "https://jyt.example.gov.cn/",
            "/uploads/logo.png",
            20,
            true,
            "主站链接",
            7L
        );
        when(cmsFriendLinkMapper.lastInsertId()).thenReturn(8L);

        assertThat(repository.create(create)).isEqualTo(8L);

        ArgumentCaptor<CmsFriendLinkWriteRow> rowCaptor =
            ArgumentCaptor.forClass(CmsFriendLinkWriteRow.class);
        verify(cmsFriendLinkMapper).insert(rowCaptor.capture());
        assertThat(rowCaptor.getValue().siteType()).isEqualTo("MAIN_SITE");
        assertThat(rowCaptor.getValue().logoUrl()).isEqualTo("/uploads/logo.png");

        UpdateCmsFriendLink update = new UpdateCmsFriendLink(
            8L,
            SiteScope.RECRUIT_SITE,
            "招生考试院",
            "https://zsks.example.gov.cn/",
            null,
            30,
            false,
            null,
            9L
        );
        when(cmsFriendLinkMapper.update(org.mockito.ArgumentMatchers.eq(8L), any())).thenReturn(1);
        assertThat(repository.update(update)).isTrue();
    }

    @Test
    void delegatesStatusSortAndDelete() {
        when(cmsFriendLinkMapper.updateStatus(8L, 0, 9L)).thenReturn(1);
        when(cmsFriendLinkMapper.delete(8L, 9L)).thenReturn(1);

        assertThat(repository.updateStatus(8L, false, 9L)).isTrue();
        repository.updateSort(List.of(new FriendLinkSortItem(8L, 5)), 9L);
        assertThat(repository.delete(8L, 9L)).isTrue();

        verify(cmsFriendLinkMapper).updateSort(8L, 5, 9L);
    }

    private CmsFriendLinkRow row(LocalDateTime now) {
        return new CmsFriendLinkRow(
            1L,
            "GLOBAL",
            "教育部",
            "https://www.moe.gov.cn/",
            null,
            10,
            1,
            "默认友情链接示例",
            now,
            now
        );
    }
}
