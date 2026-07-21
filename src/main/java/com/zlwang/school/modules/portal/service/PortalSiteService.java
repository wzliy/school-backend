package com.zlwang.school.modules.portal.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.link.repository.CmsFriendLinkRepository;
import com.zlwang.school.modules.portal.vo.PortalBannerResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnTreeNodeResponse;
import com.zlwang.school.modules.portal.vo.PortalFriendLinkResponse;
import com.zlwang.school.modules.portal.vo.PortalSiteConfigResponse;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortalSiteService {

    private static final int DEFAULT_FRIEND_LINK_LIMIT = 100;
    private static final Comparator<CmsColumn> COLUMN_ORDER = Comparator
        .comparingInt(CmsColumn::sortNo)
        .thenComparingLong(CmsColumn::id);

    private final CmsColumnRepository cmsColumnRepository;
    private final CmsContentRepository cmsContentRepository;
    private final CmsBannerRepository cmsBannerRepository;
    private final CmsFriendLinkRepository cmsFriendLinkRepository;
    private final CmsSiteConfigRepository cmsSiteConfigRepository;
    private final Clock clock;

    @Autowired
    public PortalSiteService(
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        CmsBannerRepository cmsBannerRepository,
        CmsFriendLinkRepository cmsFriendLinkRepository,
        CmsSiteConfigRepository cmsSiteConfigRepository
    ) {
        this(
            cmsColumnRepository,
            cmsContentRepository,
            cmsBannerRepository,
            cmsFriendLinkRepository,
            cmsSiteConfigRepository,
            Clock.systemDefaultZone()
        );
    }

    PortalSiteService(
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        CmsBannerRepository cmsBannerRepository,
        CmsFriendLinkRepository cmsFriendLinkRepository,
        CmsSiteConfigRepository cmsSiteConfigRepository,
        Clock clock
    ) {
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsContentRepository = cmsContentRepository;
        this.cmsBannerRepository = cmsBannerRepository;
        this.cmsFriendLinkRepository = cmsFriendLinkRepository;
        this.cmsSiteConfigRepository = cmsSiteConfigRepository;
        this.clock = clock;
    }

    public PortalSiteConfigResponse findSiteConfig(SiteType siteType) {
        return new PortalSiteConfigResponse(siteType, mergedSiteConfig(siteType));
    }

    public List<PortalColumnTreeNodeResponse> findNavigation(SiteType siteType) {
        return columnTree(siteType, true);
    }

    List<PortalColumnTreeNodeResponse> findColumnTree(SiteType siteType) {
        return columnTree(siteType, false);
    }

    private List<PortalColumnTreeNodeResponse> columnTree(
        SiteType siteType,
        boolean navigationOnly
    ) {
        List<CmsColumn> visible = columns(siteType).values().stream()
            .filter(CmsColumn::enabled)
            .filter(column -> !navigationOnly || column.navVisible())
            .sorted(COLUMN_ORDER)
            .toList();
        Map<Long, CmsColumn> byId = visible.stream()
            .collect(Collectors.toMap(CmsColumn::id, Function.identity()));
        Map<Long, List<CmsColumn>> children = new HashMap<>();
        List<CmsColumn> roots = new ArrayList<>();
        for (CmsColumn column : visible) {
            if (column.parentId() == 0 || !byId.containsKey(column.parentId())) {
                roots.add(column);
            } else {
                children.computeIfAbsent(column.parentId(), ignored -> new ArrayList<>()).add(column);
            }
        }
        roots.sort(COLUMN_ORDER);
        children.values().forEach(items -> items.sort(COLUMN_ORDER));
        return roots.stream()
            .map(root -> columnNode(root, children, new HashSet<>()))
            .toList();
    }

    public List<PortalBannerResponse> findBanners(
        SiteType siteType,
        BannerPosition position
    ) {
        validatePosition(siteType, position);
        LocalDateTime now = currentTime();
        return findBanners(siteType, position, columns(siteType), now);
    }

    public List<PortalFriendLinkResponse> findFriendLinks(SiteType siteType) {
        return findFriendLinks(siteType, DEFAULT_FRIEND_LINK_LIMIT);
    }

    LocalDateTime currentTime() {
        return LocalDateTime.now(clock);
    }

    Map<Long, CmsColumn> columns(SiteType siteType) {
        return cmsColumnRepository.findAll(siteType).stream()
            .collect(Collectors.toMap(CmsColumn::id, Function.identity()));
    }

    Map<String, String> mergedSiteConfig(SiteType siteType) {
        Map<String, String> values = new LinkedHashMap<>();
        appendConfig(values, cmsSiteConfigRepository.findAll(SiteScope.GLOBAL));
        appendConfig(values, cmsSiteConfigRepository.findAll(SiteScope.valueOf(siteType.name())));
        return values;
    }

    List<PortalBannerResponse> findBanners(
        SiteType siteType,
        BannerPosition position,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        validatePosition(siteType, position);
        return cmsBannerRepository.findActive(siteType, position, now).stream()
            .filter(banner -> targetAvailable(banner, siteType, columns, now))
            .map(PortalBannerResponse::from)
            .toList();
    }

    List<PortalColumnResponse> quickLinks(
        Map<Long, CmsColumn> columns,
        int limit
    ) {
        return navigationColumns(columns, limit).stream()
            .map(PortalColumnResponse::from)
            .toList();
    }

    List<PortalFriendLinkResponse> findFriendLinks(SiteType siteType, int limit) {
        SiteScope scope = SiteScope.valueOf(siteType.name());
        return cmsFriendLinkRepository.findEnabledForSite(scope, limit).stream()
            .map(PortalFriendLinkResponse::from)
            .toList();
    }

    boolean publicColumn(CmsColumn column, SiteType siteType) {
        return column != null && column.siteType() == siteType && column.enabled();
    }

    private List<CmsColumn> navigationColumns(Map<Long, CmsColumn> columns, int limit) {
        return columns.values().stream()
            .filter(CmsColumn::enabled)
            .filter(CmsColumn::navVisible)
            .sorted(COLUMN_ORDER)
            .limit(limit)
            .toList();
    }

    private PortalColumnTreeNodeResponse columnNode(
        CmsColumn column,
        Map<Long, List<CmsColumn>> children,
        Set<Long> ancestors
    ) {
        if (!ancestors.add(column.id())) {
            return PortalColumnTreeNodeResponse.from(column, List.of());
        }
        List<PortalColumnTreeNodeResponse> childNodes = children
            .getOrDefault(column.id(), List.of())
            .stream()
            .map(child -> columnNode(child, children, new HashSet<>(ancestors)))
            .toList();
        return PortalColumnTreeNodeResponse.from(column, childNodes);
    }

    private boolean targetAvailable(
        CmsBanner banner,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        if (banner.linkType() == BannerLinkType.NONE
            || banner.linkType() == BannerLinkType.EXTERNAL) {
            return true;
        }
        if (banner.linkRefId() == null) {
            return false;
        }
        if (banner.linkType() == BannerLinkType.COLUMN) {
            return publicColumn(columns.get(banner.linkRefId()), siteType);
        }
        return cmsContentRepository.findById(banner.linkRefId())
            .filter(content -> publicContent(content, siteType, columns, now))
            .isPresent();
    }

    private boolean publicContent(
        CmsContent content,
        SiteType siteType,
        Map<Long, CmsColumn> columns,
        LocalDateTime now
    ) {
        return content.siteType() == siteType
            && content.status() == ContentStatus.PUBLISHED
            && content.publishAt() != null
            && !content.publishAt().isAfter(now)
            && publicColumn(columns.get(content.columnId()), siteType);
    }

    private void validatePosition(SiteType siteType, BannerPosition position) {
        boolean compatible = position == BannerPosition.COLUMN
            || position == BannerPosition.HOME && siteType == SiteType.MAIN_SITE
            || position == BannerPosition.RECRUIT_HOME && siteType == SiteType.RECRUIT_SITE;
        if (!compatible) {
            throw new BusinessException(
                ErrorCode.PARAM_VALIDATION_FAILED,
                "Banner 位置与站点不匹配"
            );
        }
    }

    private void appendConfig(Map<String, String> values, List<CmsSiteConfig> configs) {
        configs.forEach(config -> values.put(config.configKey(), config.configValue()));
    }
}
