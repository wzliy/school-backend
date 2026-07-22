package com.zlwang.school.infrastructure.persistence.local;

import static org.assertj.core.api.Assertions.assertThat;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.repository.CreateCmsBanner;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocalCmsStorePublicQueryTests {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 21, 10, 0);

    @Test
    void activeBannersIncludeTimeBoundariesAndExcludeFutureOrExpiredRows() {
        LocalCmsStore store = new LocalCmsStore();
        long startsNow = store.createBanner(banner("开始边界", NOW, NOW.plusHours(1)));
        long endsNow = store.createBanner(banner("结束边界", NOW.minusHours(1), NOW));
        store.createBanner(banner("尚未开始", NOW.plusSeconds(1), null));
        store.createBanner(banner("已经结束", null, NOW.minusSeconds(1)));

        assertThat(store.findActiveBanners(
            SiteType.MAIN_SITE,
            BannerPosition.HOME,
            NOW
        )).extracting(banner -> banner.id())
            .containsExactly(startsNow, endsNow);
    }

    @Test
    void searchAndViewCountIncludePublishBoundaryAndExcludeFutureContent() {
        LocalCmsStore store = new LocalCmsStore();
        long effectiveId = store.createContent(content("发布时间边界内容"));
        long futureId = store.createContent(content("发布时间边界未来内容"));
        store.publishContent(effectiveId, NOW);
        store.publishContent(futureId, NOW.plusSeconds(1));

        PageResult<CmsContent> page = store.searchPublishedContents(
            "发布时间边界",
            SiteType.MAIN_SITE,
            101L,
            NOW,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).extracting(CmsContent::id).containsExactly(effectiveId);
        assertThat(store.incrementPublishedContentViewCount(effectiveId, NOW)).hasValue(1);
        assertThat(store.incrementPublishedContentViewCount(futureId, NOW)).isEmpty();
    }

    private CreateCmsBanner banner(
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        return new CreateCmsBanner(
            SiteType.MAIN_SITE,
            BannerPosition.HOME,
            title,
            null,
            "/uploads/" + title + ".jpg",
            null,
            BannerLinkType.NONE,
            null,
            null,
            BannerLinkTarget._self,
            10,
            true,
            startTime,
            endTime,
            null,
            1L
        );
    }

    private CreateCmsContent content(String title) {
        return new CreateCmsContent(
            101L,
            SiteType.MAIN_SITE,
            title,
            null,
            "摘要",
            "<p>正文</p>",
            null,
            null,
            null,
            null,
            ContentStatus.DRAFT,
            false,
            false,
            10,
            null,
            null,
            null,
            Map.of(),
            List.of(),
            1L
        );
    }
}
