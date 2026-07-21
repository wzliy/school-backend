package com.zlwang.school.infrastructure.persistence.banner;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.banner.dto.BannerSortItem;
import com.zlwang.school.modules.banner.model.BannerLinkTarget;
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
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisCmsBannerRepository implements CmsBannerRepository {

    private final CmsBannerMapper cmsBannerMapper;

    public MybatisCmsBannerRepository(CmsBannerMapper cmsBannerMapper) {
        this.cmsBannerMapper = cmsBannerMapper;
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
        String site = name(siteType);
        String placement = name(position);
        Integer enabledFlag = enabled == null ? null : flag(enabled);
        long total = cmsBannerMapper.countBanners(keyword, site, placement, enabledFlag);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<CmsBanner> records = cmsBannerMapper.findBanners(
            keyword,
            site,
            placement,
            enabledFlag,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(this::toBanner).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public Optional<CmsBanner> findById(long id) {
        return Optional.ofNullable(cmsBannerMapper.findById(id)).map(this::toBanner);
    }

    @Override
    public List<CmsBanner> findActive(
        SiteType siteType,
        BannerPosition position,
        LocalDateTime effectiveAt
    ) {
        return cmsBannerMapper.findActive(siteType.name(), position.name(), effectiveAt).stream()
            .map(this::toBanner)
            .toList();
    }

    @Override
    @Transactional
    public long create(CreateCmsBanner command) {
        cmsBannerMapper.insert(writeRow(command));
        return cmsBannerMapper.lastInsertId();
    }

    @Override
    public boolean update(UpdateCmsBanner command) {
        return cmsBannerMapper.update(command.id(), writeRow(command)) > 0;
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return cmsBannerMapper.updateStatus(id, flag(enabled), operatorId) > 0;
    }

    @Override
    @Transactional
    public void updateSort(List<BannerSortItem> items, long operatorId) {
        items.forEach(item -> cmsBannerMapper.updateSort(item.id(), item.sortNo(), operatorId));
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return cmsBannerMapper.delete(id, operatorId) > 0;
    }

    @Override
    public long countReferences(BannerLinkType linkType, long linkRefId, boolean enabledOnly) {
        return cmsBannerMapper.countReferences(linkType.name(), linkRefId, enabledOnly);
    }

    private CmsBannerWriteRow writeRow(CreateCmsBanner command) {
        return new CmsBannerWriteRow(
            command.siteType().name(),
            command.position().name(),
            command.title(),
            command.subtitle(),
            command.imageUrl(),
            command.mobileImageUrl(),
            command.linkType().name(),
            command.linkRefId(),
            command.linkUrl(),
            command.linkTarget().name(),
            command.sortNo(),
            flag(command.enabled()),
            command.startTime(),
            command.endTime(),
            command.remark(),
            command.operatorId()
        );
    }

    private CmsBannerWriteRow writeRow(UpdateCmsBanner command) {
        return new CmsBannerWriteRow(
            command.siteType().name(),
            command.position().name(),
            command.title(),
            command.subtitle(),
            command.imageUrl(),
            command.mobileImageUrl(),
            command.linkType().name(),
            command.linkRefId(),
            command.linkUrl(),
            command.linkTarget().name(),
            command.sortNo(),
            flag(command.enabled()),
            command.startTime(),
            command.endTime(),
            command.remark(),
            command.operatorId()
        );
    }

    private CmsBanner toBanner(CmsBannerRow row) {
        return new CmsBanner(
            row.id(),
            SiteType.valueOf(row.siteType()),
            BannerPosition.valueOf(row.position()),
            row.title(),
            row.subtitle(),
            row.imageUrl(),
            row.mobileImageUrl(),
            BannerLinkType.valueOf(row.linkType()),
            row.linkRefId(),
            row.linkUrl(),
            BannerLinkTarget.valueOf(row.linkTarget()),
            row.sortNo(),
            row.enabled() == 1,
            row.startTime(),
            row.endTime(),
            row.remark(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private int flag(boolean value) {
        return value ? 1 : 0;
    }
}
