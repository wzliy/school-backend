package com.zlwang.school.infrastructure.persistence.link;

import com.zlwang.school.common.api.PageResult;
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
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisCmsFriendLinkRepository implements CmsFriendLinkRepository {

    private final CmsFriendLinkMapper cmsFriendLinkMapper;

    public MybatisCmsFriendLinkRepository(CmsFriendLinkMapper cmsFriendLinkMapper) {
        this.cmsFriendLinkMapper = cmsFriendLinkMapper;
    }

    @Override
    public PageResult<CmsFriendLink> findPage(
        String keyword,
        SiteScope siteType,
        Boolean enabled,
        long pageNo,
        long pageSize
    ) {
        String scope = siteType == null ? null : siteType.name();
        Integer enabledFlag = enabled == null ? null : flag(enabled);
        long total = cmsFriendLinkMapper.countFriendLinks(keyword, scope, enabledFlag);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<CmsFriendLink> records = cmsFriendLinkMapper.findFriendLinks(
            keyword,
            scope,
            enabledFlag,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(this::toFriendLink).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public Optional<CmsFriendLink> findById(long id) {
        return Optional.ofNullable(cmsFriendLinkMapper.findById(id)).map(this::toFriendLink);
    }

    @Override
    @Transactional
    public long create(CreateCmsFriendLink command) {
        cmsFriendLinkMapper.insert(writeRow(command));
        return cmsFriendLinkMapper.lastInsertId();
    }

    @Override
    public boolean update(UpdateCmsFriendLink command) {
        return cmsFriendLinkMapper.update(command.id(), writeRow(command)) > 0;
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return cmsFriendLinkMapper.updateStatus(id, flag(enabled), operatorId) > 0;
    }

    @Override
    @Transactional
    public void updateSort(List<FriendLinkSortItem> items, long operatorId) {
        items.forEach(item -> cmsFriendLinkMapper.updateSort(item.id(), item.sortNo(), operatorId));
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return cmsFriendLinkMapper.delete(id, operatorId) > 0;
    }

    private CmsFriendLinkWriteRow writeRow(CreateCmsFriendLink command) {
        return new CmsFriendLinkWriteRow(
            command.siteType().name(),
            command.name(),
            command.linkUrl(),
            command.logoUrl(),
            command.sortNo(),
            flag(command.enabled()),
            command.remark(),
            command.operatorId()
        );
    }

    private CmsFriendLinkWriteRow writeRow(UpdateCmsFriendLink command) {
        return new CmsFriendLinkWriteRow(
            command.siteType().name(),
            command.name(),
            command.linkUrl(),
            command.logoUrl(),
            command.sortNo(),
            flag(command.enabled()),
            command.remark(),
            command.operatorId()
        );
    }

    private CmsFriendLink toFriendLink(CmsFriendLinkRow row) {
        return new CmsFriendLink(
            row.id(),
            SiteScope.valueOf(row.siteType()),
            row.name(),
            row.linkUrl(),
            row.logoUrl(),
            row.sortNo(),
            row.enabled() == 1,
            row.remark(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private int flag(boolean value) {
        return value ? 1 : 0;
    }
}
