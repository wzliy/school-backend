package com.zlwang.school.modules.site.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.site.dto.UpdateSiteConfigItem;
import com.zlwang.school.modules.site.dto.UpdateSiteConfigsRequest;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteConfigType;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.site.repository.UpdateSiteConfigValue;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Service
public class CmsSiteConfigService {

    private final CmsSiteConfigRepository cmsSiteConfigRepository;
    private final ObjectMapper objectMapper;

    public CmsSiteConfigService(
        CmsSiteConfigRepository cmsSiteConfigRepository,
        ObjectMapper objectMapper
    ) {
        this.cmsSiteConfigRepository = cmsSiteConfigRepository;
        this.objectMapper = objectMapper;
    }

    public List<CmsSiteConfig> findAll(SiteScope siteType) {
        return cmsSiteConfigRepository.findAll(siteType);
    }

    @Transactional
    public void update(SiteScope siteType, UpdateSiteConfigsRequest request, long operatorId) {
        List<NormalizedItem> items = request.items().stream()
            .map(item -> new NormalizedItem(item.configKey().trim(), item.configValue().trim()))
            .toList();
        if (items.stream().map(NormalizedItem::configKey).distinct().count() != items.size()) {
            throw badRequest("配置项包含重复配置键");
        }

        Map<String, CmsSiteConfig> existing = cmsSiteConfigRepository.findAll(siteType).stream()
            .collect(Collectors.toMap(CmsSiteConfig::configKey, Function.identity()));
        for (NormalizedItem item : items) {
            CmsSiteConfig config = existing.get(item.configKey());
            if (config == null) {
                throw badRequest("站点配置项不存在：" + item.configKey());
            }
            validateValue(config, item.configValue());
        }

        List<UpdateSiteConfigValue> values = items.stream()
            .map(item -> new UpdateSiteConfigValue(item.configKey(), item.configValue()))
            .toList();
        if (!cmsSiteConfigRepository.updateValues(siteType, values, operatorId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "站点配置已发生变化，请刷新后重试");
        }
    }

    private void validateValue(CmsSiteConfig config, String value) {
        switch (config.configType()) {
            case STRING -> {
                if (value.length() > 2_000) {
                    throw badRequest("字符串配置值长度不能超过 2000 个字符");
                }
            }
            case NUMBER -> validateNumber(config.configKey(), value);
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw badRequest("布尔配置值必须是 true 或 false");
                }
            }
            case JSON -> validateJson(value);
            case IMAGE -> validateImageUrl(value);
        }
    }

    private void validateNumber(String configKey, String value) {
        if (!StringUtils.hasText(value)) {
            throw badRequest("数字配置值不能为空");
        }
        if (value.length() > 64) {
            throw badRequest("数字配置值格式不正确");
        }
        try {
            BigDecimal number = new BigDecimal(value);
            if (isHomeLimit(configKey)
                && (number.scale() > 0 || number.intValueExact() < 1 || number.intValueExact() > 100)) {
                throw badRequest("首页展示数量必须是 1-100 的整数");
            }
        } catch (ArithmeticException ex) {
            throw badRequest("首页展示数量必须是 1-100 的整数");
        } catch (NumberFormatException ex) {
            throw badRequest("数字配置值格式不正确");
        }
    }

    private boolean isHomeLimit(String configKey) {
        return "homeNewsLimit".equals(configKey) || "homeNoticeLimit".equals(configKey);
    }

    private void validateJson(String value) {
        if (!StringUtils.hasText(value)) {
            throw badRequest("JSON 配置值不能为空");
        }
        try {
            objectMapper.readTree(value);
        } catch (Exception ex) {
            throw badRequest("JSON 配置值格式不正确");
        }
    }

    private void validateImageUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
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
        try {
            URI uri = URI.create(normalized);
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost() != null) {
                return;
            }
        } catch (IllegalArgumentException ex) {
            // Fall through to the common validation error.
        }
        throw badRequest("图片配置值必须是站内相对地址或有效的 HTTP/HTTPS 地址");
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private record NormalizedItem(String configKey, String configValue) {
    }
}
