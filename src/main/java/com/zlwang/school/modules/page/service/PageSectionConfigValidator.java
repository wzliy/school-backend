package com.zlwang.school.modules.page.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSectionType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PageSectionConfigValidator {

    public Map<String, Object> validate(
        PageCode pageCode,
        PageSectionType sectionType,
        Map<String, Object> input
    ) {
        Map<String, Object> source = input == null ? Map.of() : input;
        Map<String, Object> normalized = new LinkedHashMap<>();
        switch (sectionType) {
            case HERO_BANNER -> validateHero(pageCode, source, normalized);
            case CONTENT_FEED -> {
                rejectUnknown(source, Set.of("showSummary", "showCover", "moreLinkText"));
                optionalBoolean(source, normalized, "showSummary");
                optionalBoolean(source, normalized, "showCover");
                optionalText(source, normalized, "moreLinkText", 16);
            }
            case QUICK_LINKS -> {
                rejectUnknown(source, Set.of("showDescription", "maxRows"));
                optionalBoolean(source, normalized, "showDescription");
                optionalInteger(source, normalized, "maxRows", 1, 4);
            }
            case IMAGE_GALLERY -> {
                rejectUnknown(source, Set.of("aspectRatio", "showCaption"));
                optionalChoice(source, normalized, "aspectRatio", Set.of("4:3", "16:9", "1:1"));
                optionalBoolean(source, normalized, "showCaption");
            }
            case FRIEND_LINKS -> {
                rejectUnknown(source, Set.of("showLogo", "openInNewWindow"));
                optionalBoolean(source, normalized, "showLogo");
                optionalBoolean(source, normalized, "openInNewWindow");
            }
            case CONTACT_INFO -> {
                rejectUnknown(source, Set.of("showPhone", "showEmail", "showAddress"));
                optionalBoolean(source, normalized, "showPhone");
                optionalBoolean(source, normalized, "showEmail");
                optionalBoolean(source, normalized, "showAddress");
            }
        }
        return Collections.unmodifiableMap(normalized);
    }

    private void validateHero(
        PageCode pageCode,
        Map<String, Object> source,
        Map<String, Object> normalized
    ) {
        rejectUnknown(source, Set.of("bannerPosition", "autoplay", "intervalSeconds"));
        Object position = source.get("bannerPosition");
        if (!(position instanceof String text) || !pageCode.name().equals(text)) {
            throw badRequest("config.bannerPosition 必须与页面编码一致");
        }
        normalized.put("bannerPosition", pageCode.name());
        optionalBoolean(source, normalized, "autoplay");
        optionalInteger(source, normalized, "intervalSeconds", 3, 10);
    }

    private void optionalBoolean(Map<String, Object> source, Map<String, Object> target, String key) {
        if (!source.containsKey(key) || source.get(key) == null) {
            return;
        }
        if (!(source.get(key) instanceof Boolean value)) {
            throw badRequest("config." + key + " 必须是布尔值");
        }
        target.put(key, value);
    }

    private void optionalInteger(
        Map<String, Object> source,
        Map<String, Object> target,
        String key,
        int min,
        int max
    ) {
        if (!source.containsKey(key) || source.get(key) == null) {
            return;
        }
        Object raw = source.get(key);
        if (!(raw instanceof Number number)
            || number.doubleValue() != number.longValue()
            || number.longValue() < min
            || number.longValue() > max) {
            throw badRequest("config." + key + " 必须是 " + min + "-" + max + " 之间的整数");
        }
        target.put(key, number.longValue());
    }

    private void optionalText(
        Map<String, Object> source,
        Map<String, Object> target,
        String key,
        int maxLength
    ) {
        if (!source.containsKey(key) || source.get(key) == null) {
            return;
        }
        if (!(source.get(key) instanceof String text)) {
            throw badRequest("config." + key + " 必须是字符串");
        }
        String value = StringUtils.hasText(text) ? text.trim() : null;
        if (value == null) {
            return;
        }
        if (value.length() > maxLength) {
            throw badRequest("config." + key + " 长度不能超过 " + maxLength);
        }
        if (value.contains("<") || value.contains(">")) {
            throw badRequest("config." + key + " 不能包含 HTML");
        }
        target.put(key, value);
    }

    private void optionalChoice(
        Map<String, Object> source,
        Map<String, Object> target,
        String key,
        Set<String> choices
    ) {
        if (!source.containsKey(key) || source.get(key) == null) {
            return;
        }
        if (!(source.get(key) instanceof String value) || !choices.contains(value)) {
            throw badRequest("config." + key + " 不是允许的选项");
        }
        target.put(key, value);
    }

    private void rejectUnknown(Map<String, Object> source, Set<String> allowed) {
        source.keySet().stream()
            .filter(key -> !allowed.contains(key))
            .findFirst()
            .ifPresent(key -> {
                throw badRequest("config 包含未定义字段：" + key);
            });
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }
}
