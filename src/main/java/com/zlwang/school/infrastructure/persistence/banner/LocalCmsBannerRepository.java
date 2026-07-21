package com.zlwang.school.infrastructure.persistence.banner;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.banner.dto.BannerSortItem;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.banner.repository.CreateCmsBanner;
import com.zlwang.school.modules.banner.repository.UpdateCmsBanner;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsBannerRepository implements CmsBannerRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsBannerRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public PageResult<CmsBanner> findPage(
        String keyword,
        SiteType siteType,
        BannerPosition position,
        Boolean enabled,
        long pageNo,
        long pageSize
    ) {
        return localCmsStore.findBanners(keyword, siteType, position, enabled, pageNo, pageSize);
    }

    @Override
    public Optional<CmsBanner> findById(long id) {
        return localCmsStore.findBanner(id);
    }

    @Override
    public List<CmsBanner> findActive(
        SiteType siteType,
        BannerPosition position,
        LocalDateTime effectiveAt
    ) {
        return localCmsStore.findActiveBanners(siteType, position, effectiveAt);
    }

    @Override
    public long create(CreateCmsBanner command) {
        return localCmsStore.createBanner(command);
    }

    @Override
    public boolean update(UpdateCmsBanner command) {
        return localCmsStore.updateBanner(command);
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return localCmsStore.updateBannerStatus(id, enabled);
    }

    @Override
    public void updateSort(List<BannerSortItem> items, long operatorId) {
        localCmsStore.updateBannerSort(items);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localCmsStore.deleteBanner(id);
    }

    @Override
    public long countReferences(BannerLinkType linkType, long linkRefId, boolean enabledOnly) {
        return localCmsStore.countBannerReferences(linkType, linkRefId, enabledOnly);
    }
}
