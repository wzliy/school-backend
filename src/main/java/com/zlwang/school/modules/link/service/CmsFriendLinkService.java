package com.zlwang.school.modules.link.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.link.dto.CreateFriendLinkRequest;
import com.zlwang.school.modules.link.dto.FriendLinkPageQuery;
import com.zlwang.school.modules.link.dto.FriendLinkSortItem;
import com.zlwang.school.modules.link.dto.SortFriendLinksRequest;
import com.zlwang.school.modules.link.dto.UpdateFriendLinkRequest;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.link.repository.CmsFriendLinkRepository;
import com.zlwang.school.modules.link.repository.CreateCmsFriendLink;
import com.zlwang.school.modules.link.repository.UpdateCmsFriendLink;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmsFriendLinkService {

    private static final int MAX_SORT_NO = 999_999;

    private final CmsFriendLinkRepository cmsFriendLinkRepository;

    public CmsFriendLinkService(CmsFriendLinkRepository cmsFriendLinkRepository) {
        this.cmsFriendLinkRepository = cmsFriendLinkRepository;
    }

    public PageResult<CmsFriendLink> findPage(FriendLinkPageQuery query) {
        return cmsFriendLinkRepository.findPage(
            normalize(query.getKeyword()),
            query.getSiteType(),
            query.getEnabled(),
            query.getPageNo(),
            query.getPageSize()
        );
    }

    public CmsFriendLink findById(long id) {
        return requiredLink(id);
    }

    public long create(CreateFriendLinkRequest request, long operatorId) {
        validate(request.linkUrl(), request.logoUrl(), request.sortNo());
        return cmsFriendLinkRepository.create(new CreateCmsFriendLink(
            request.siteType(),
            request.name().trim(),
            request.linkUrl().trim(),
            normalize(request.logoUrl()),
            request.sortNo(),
            request.enabled(),
            normalize(request.remark()),
            operatorId
        ));
    }

    public void update(long id, UpdateFriendLinkRequest request, long operatorId) {
        requiredLink(id);
        validate(request.linkUrl(), request.logoUrl(), request.sortNo());
        if (!cmsFriendLinkRepository.update(new UpdateCmsFriendLink(
            id,
            request.siteType(),
            request.name().trim(),
            request.linkUrl().trim(),
            normalize(request.logoUrl()),
            request.sortNo(),
            request.enabled(),
            normalize(request.remark()),
            operatorId
        ))) {
            throw notFound(id);
        }
    }

    public void updateStatus(long id, boolean enabled, long operatorId) {
        requiredLink(id);
        if (!cmsFriendLinkRepository.updateStatus(id, enabled, operatorId)) {
            throw notFound(id);
        }
    }

    public void updateSort(SortFriendLinksRequest request, long operatorId) {
        List<FriendLinkSortItem> items = request.items();
        if (items.stream().map(FriendLinkSortItem::id).distinct().count() != items.size()) {
            throw badRequest("排序列表包含重复友情链接");
        }
        for (FriendLinkSortItem item : items) {
            if (item.sortNo() > MAX_SORT_NO) {
                throw badRequest("sortNo 不能大于 " + MAX_SORT_NO);
            }
            requiredLink(item.id());
        }
        cmsFriendLinkRepository.updateSort(items, operatorId);
    }

    public void delete(long id, long operatorId) {
        requiredLink(id);
        if (!cmsFriendLinkRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private void validate(String linkUrl, String logoUrl, int sortNo) {
        if (sortNo > MAX_SORT_NO) {
            throw badRequest("sortNo 不能大于 " + MAX_SORT_NO);
        }
        validateHttpUrl(linkUrl, "链接地址");
        if (StringUtils.hasText(logoUrl)) {
            validateLogoUrl(logoUrl);
        }
    }

    private void validateLogoUrl(String value) {
        String normalized = value.trim();
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")
            && !normalized.startsWith("//")
            && !normalized.contains("..")
            && !normalized.contains("\\")
            && !lowerCase.contains("%2e")
            && !lowerCase.contains("%5c")) {
            return;
        }
        validateHttpUrl(normalized, "Logo 地址");
    }

    private void validateHttpUrl(String value, String fieldName) {
        try {
            URI uri = URI.create(value.trim());
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost() != null) {
                return;
            }
        } catch (IllegalArgumentException ex) {
            // Fall through to the common validation error.
        }
        throw badRequest(fieldName + "必须是有效的 HTTP 或 HTTPS 地址");
    }

    private CmsFriendLink requiredLink(long id) {
        return cmsFriendLinkRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "友情链接不存在：" + id);
    }
}
