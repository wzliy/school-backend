package com.zlwang.school.modules.seo.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SeoMetadataService {

    private static final String DEFAULT_TITLE = "defaultSeoTitle";
    private static final String DEFAULT_KEYWORDS = "defaultSeoKeywords";
    private static final String DEFAULT_DESCRIPTION = "defaultSeoDescription";

    private final CmsColumnRepository cmsColumnRepository;
    private final CmsContentRepository cmsContentRepository;
    private final CmsSiteConfigRepository cmsSiteConfigRepository;

    public SeoMetadataService(
        CmsColumnRepository cmsColumnRepository,
        CmsContentRepository cmsContentRepository,
        CmsSiteConfigRepository cmsSiteConfigRepository
    ) {
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsContentRepository = cmsContentRepository;
        this.cmsSiteConfigRepository = cmsSiteConfigRepository;
    }

    public SeoMetadata resolveColumn(long columnId) {
        CmsColumn column = cmsColumnRepository.findById(columnId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "栏目不存在：" + columnId));
        Map<String, String> defaults = defaults(column.siteType());
        return new SeoMetadata(
            first(column.seoTitle(), column.columnName(), defaults.get(DEFAULT_TITLE)),
            first(column.seoKeywords(), defaults.get(DEFAULT_KEYWORDS)),
            first(column.seoDescription(), defaults.get(DEFAULT_DESCRIPTION)),
            columnCanonicalPath(column)
        );
    }

    public SeoMetadata resolveContent(long contentId) {
        CmsContent content = cmsContentRepository.findById(contentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "内容不存在：" + contentId));
        CmsColumn column = cmsColumnRepository.findById(content.columnId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "栏目不存在：" + content.columnId()));
        Map<String, String> defaults = defaults(content.siteType());
        return new SeoMetadata(
            first(
                content.seoTitle(),
                column.seoTitle(),
                content.title(),
                column.columnName(),
                defaults.get(DEFAULT_TITLE)
            ),
            first(content.seoKeywords(), column.seoKeywords(), defaults.get(DEFAULT_KEYWORDS)),
            first(
                content.seoDescription(),
                column.seoDescription(),
                content.summary(),
                defaults.get(DEFAULT_DESCRIPTION)
            ),
            contentCanonicalPath(column, content.id())
        );
    }

    private Map<String, String> defaults(SiteType siteType) {
        SiteScope scope = SiteScope.valueOf(siteType.name());
        Map<String, String> values = new LinkedHashMap<>();
        for (CmsSiteConfig config : cmsSiteConfigRepository.findAll(scope)) {
            values.putIfAbsent(config.configKey(), config.configValue());
        }
        return values;
    }

    private String columnCanonicalPath(CmsColumn column) {
        return StringUtils.hasText(column.routePath())
            ? normalizePath(column.routePath())
            : "/columns/" + column.id();
    }

    private String contentCanonicalPath(CmsColumn column, long contentId) {
        return columnCanonicalPath(column) + "/" + contentId;
    }

    private String normalizePath(String value) {
        String path = value.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String first(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
