package com.zlwang.school.modules.link.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.link.dto.FriendLinkSortItem;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.site.model.SiteScope;
import java.util.List;
import java.util.Optional;

public interface CmsFriendLinkRepository {

    PageResult<CmsFriendLink> findPage(
        String keyword,
        SiteScope siteType,
        Boolean enabled,
        long pageNo,
        long pageSize
    );

    Optional<CmsFriendLink> findById(long id);

    List<CmsFriendLink> findEnabledForSite(SiteScope siteType, int limit);

    long create(CreateCmsFriendLink command);

    boolean update(UpdateCmsFriendLink command);

    boolean updateStatus(long id, boolean enabled, long operatorId);

    void updateSort(List<FriendLinkSortItem> items, long operatorId);

    boolean delete(long id, long operatorId);
}
