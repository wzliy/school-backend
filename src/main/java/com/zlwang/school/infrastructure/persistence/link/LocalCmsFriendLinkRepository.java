package com.zlwang.school.infrastructure.persistence.link;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.link.dto.FriendLinkSortItem;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.link.repository.CmsFriendLinkRepository;
import com.zlwang.school.modules.link.repository.CreateCmsFriendLink;
import com.zlwang.school.modules.link.repository.UpdateCmsFriendLink;
import com.zlwang.school.modules.site.model.SiteScope;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsFriendLinkRepository implements CmsFriendLinkRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsFriendLinkRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public PageResult<CmsFriendLink> findPage(
        String keyword,
        SiteScope siteType,
        Boolean enabled,
        long pageNo,
        long pageSize
    ) {
        return localCmsStore.findFriendLinks(keyword, siteType, enabled, pageNo, pageSize);
    }

    @Override
    public Optional<CmsFriendLink> findById(long id) {
        return localCmsStore.findFriendLink(id);
    }

    @Override
    public long create(CreateCmsFriendLink command) {
        return localCmsStore.createFriendLink(command);
    }

    @Override
    public boolean update(UpdateCmsFriendLink command) {
        return localCmsStore.updateFriendLink(command);
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return localCmsStore.updateFriendLinkStatus(id, enabled);
    }

    @Override
    public void updateSort(List<FriendLinkSortItem> items, long operatorId) {
        localCmsStore.updateFriendLinkSort(items);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localCmsStore.deleteFriendLink(id);
    }
}
