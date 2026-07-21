package com.zlwang.school.modules.banner.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.banner.dto.BannerSortItem;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CmsBannerRepository {

    PageResult<CmsBanner> findPage(
        String keyword,
        SiteType siteType,
        BannerPosition position,
        Boolean enabled,
        long pageNo,
        long pageSize
    );

    Optional<CmsBanner> findById(long id);

    List<CmsBanner> findActive(
        SiteType siteType,
        BannerPosition position,
        LocalDateTime effectiveAt
    );

    long create(CreateCmsBanner command);

    boolean update(UpdateCmsBanner command);

    boolean updateStatus(long id, boolean enabled, long operatorId);

    void updateSort(List<BannerSortItem> items, long operatorId);

    boolean delete(long id, long operatorId);

    long countReferences(BannerLinkType linkType, long linkRefId, boolean enabledOnly);
}
