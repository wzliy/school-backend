package com.zlwang.school.modules.banner.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.banner.dto.BannerPageQuery;
import com.zlwang.school.modules.banner.dto.BannerSortItem;
import com.zlwang.school.modules.banner.dto.CreateBannerRequest;
import com.zlwang.school.modules.banner.dto.SortBannersRequest;
import com.zlwang.school.modules.banner.dto.UpdateBannerRequest;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.banner.repository.CreateCmsBanner;
import com.zlwang.school.modules.banner.repository.UpdateCmsBanner;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.template.model.SiteType;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmsBannerService {

    private static final int MAX_SORT_NO = 999_999;

    private final CmsBannerRepository cmsBannerRepository;
    private final CmsColumnRepository cmsColumnRepository;
    private final CmsContentRepository cmsContentRepository;

    public CmsBannerService(
        CmsBannerRepository cmsBannerRepository,
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository
    ) {
        this.cmsBannerRepository = cmsBannerRepository;
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsContentRepository = cmsContentRepository;
    }

    public PageResult<CmsBanner> findPage(BannerPageQuery query) {
        return cmsBannerRepository.findPage(
            normalize(query.getKeyword()),
            query.getSiteType(),
            query.getPosition(),
            query.getEnabled(),
            query.getPageNo(),
            query.getPageSize()
        );
    }

    public CmsBanner findById(long id) {
        return requiredBanner(id);
    }

    public long create(CreateBannerRequest request, long operatorId) {
        validate(
            request.siteType(),
            request.position(),
            request.linkType(),
            request.linkRefId(),
            request.linkUrl(),
            request.enabled(),
            request.startTime(),
            request.endTime(),
            request.sortNo()
        );
        return cmsBannerRepository.create(new CreateCmsBanner(
            request.siteType(),
            request.position(),
            request.title().trim(),
            normalize(request.subtitle()),
            request.imageUrl().trim(),
            normalize(request.mobileImageUrl()),
            request.linkType(),
            request.linkRefId(),
            normalize(request.linkUrl()),
            request.linkTarget(),
            request.sortNo(),
            request.enabled(),
            request.startTime(),
            request.endTime(),
            normalize(request.remark()),
            operatorId
        ));
    }

    public void update(long id, UpdateBannerRequest request, long operatorId) {
        requiredBanner(id);
        validate(
            request.siteType(),
            request.position(),
            request.linkType(),
            request.linkRefId(),
            request.linkUrl(),
            request.enabled(),
            request.startTime(),
            request.endTime(),
            request.sortNo()
        );
        if (!cmsBannerRepository.update(new UpdateCmsBanner(
            id,
            request.siteType(),
            request.position(),
            request.title().trim(),
            normalize(request.subtitle()),
            request.imageUrl().trim(),
            normalize(request.mobileImageUrl()),
            request.linkType(),
            request.linkRefId(),
            normalize(request.linkUrl()),
            request.linkTarget(),
            request.sortNo(),
            request.enabled(),
            request.startTime(),
            request.endTime(),
            normalize(request.remark()),
            operatorId
        ))) {
            throw notFound(id);
        }
    }

    public void updateStatus(long id, boolean enabled, long operatorId) {
        CmsBanner banner = requiredBanner(id);
        if (enabled) {
            validate(
                banner.siteType(),
                banner.position(),
                banner.linkType(),
                banner.linkRefId(),
                banner.linkUrl(),
                true,
                banner.startTime(),
                banner.endTime(),
                banner.sortNo()
            );
        }
        if (!cmsBannerRepository.updateStatus(id, enabled, operatorId)) {
            throw notFound(id);
        }
    }

    public void updateSort(SortBannersRequest request, long operatorId) {
        List<BannerSortItem> items = request.items();
        if (items.stream().map(BannerSortItem::id).distinct().count() != items.size()) {
            throw badRequest("排序列表包含重复 Banner");
        }
        for (BannerSortItem item : items) {
            if (item.sortNo() > MAX_SORT_NO) {
                throw badRequest("sortNo 不能大于 " + MAX_SORT_NO);
            }
            requiredBanner(item.id());
        }
        cmsBannerRepository.updateSort(items, operatorId);
    }

    public void delete(long id, long operatorId) {
        requiredBanner(id);
        if (!cmsBannerRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private void validate(
        SiteType siteType,
        BannerPosition position,
        BannerLinkType linkType,
        Long linkRefId,
        String linkUrl,
        boolean enabled,
        java.time.LocalDateTime startTime,
        java.time.LocalDateTime endTime,
        int sortNo
    ) {
        validatePosition(siteType, position);
        if (sortNo > MAX_SORT_NO) {
            throw badRequest("sortNo 不能大于 " + MAX_SORT_NO);
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw badRequest("startTime 必须早于 endTime");
        }
        validateLink(siteType, linkType, linkRefId, linkUrl, enabled);
    }

    private void validatePosition(SiteType siteType, BannerPosition position) {
        if (position == BannerPosition.HOME && siteType != SiteType.MAIN_SITE) {
            throw badRequest("HOME 位置只能属于主站");
        }
        if (position == BannerPosition.RECRUIT_HOME && siteType != SiteType.RECRUIT_SITE) {
            throw badRequest("RECRUIT_HOME 位置只能属于招生就业专题站");
        }
    }

    private void validateLink(
        SiteType siteType,
        BannerLinkType linkType,
        Long linkRefId,
        String linkUrl,
        boolean enabled
    ) {
        boolean hasUrl = StringUtils.hasText(linkUrl);
        switch (linkType) {
            case NONE -> {
                if (linkRefId != null || hasUrl) {
                    throw badRequest("NONE 跳转不能设置引用 ID 或链接地址");
                }
            }
            case EXTERNAL -> {
                if (linkRefId != null || !hasUrl) {
                    throw badRequest("EXTERNAL 跳转必须只设置链接地址");
                }
                validateExternalUrl(linkUrl);
            }
            case COLUMN -> {
                requireInternalReference(linkRefId, hasUrl, "COLUMN");
                CmsColumn column = cmsColumnRepository.findById(linkRefId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "引用栏目不存在：" + linkRefId));
                if (column.siteType() != siteType) {
                    throw badRequest("引用栏目与 Banner 必须属于同一站点");
                }
                if (enabled && !column.enabled()) {
                    throw new BusinessException(ErrorCode.CONFLICT, "引用栏目已停用，不能启用 Banner");
                }
            }
            case CONTENT -> {
                requireInternalReference(linkRefId, hasUrl, "CONTENT");
                CmsContent content = cmsContentRepository.findById(linkRefId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "引用内容不存在：" + linkRefId));
                if (content.siteType() != siteType) {
                    throw badRequest("引用内容与 Banner 必须属于同一站点");
                }
                if (enabled && content.status() != ContentStatus.PUBLISHED) {
                    throw new BusinessException(ErrorCode.CONFLICT, "引用内容未发布，不能启用 Banner");
                }
            }
        }
    }

    private void requireInternalReference(Long linkRefId, boolean hasUrl, String linkType) {
        if (linkRefId == null || hasUrl) {
            throw badRequest(linkType + " 跳转必须只设置引用 ID");
        }
    }

    private void validateExternalUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
                throw badRequest("外部链接必须是有效的 HTTP 或 HTTPS 地址");
            }
        } catch (IllegalArgumentException ex) {
            throw badRequest("外部链接必须是有效的 HTTP 或 HTTPS 地址");
        }
    }

    private CmsBanner requiredBanner(long id) {
        return cmsBannerRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "Banner 不存在：" + id);
    }
}
