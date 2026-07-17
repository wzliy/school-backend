package com.zlwang.school.modules.content.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.EditorFieldOption;
import com.zlwang.school.modules.template.model.EditorFieldType;
import com.zlwang.school.modules.template.model.FieldValidationRule;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.service.PageTemplateRegistry;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ContentTemplateValidator {

    private final PageTemplateRegistry pageTemplateRegistry;

    public ContentTemplateValidator(PageTemplateRegistry pageTemplateRegistry) {
        this.pageTemplateRegistry = pageTemplateRegistry;
    }

    public Map<String, Object> validateExtensionData(
        CmsColumn column,
        Map<String, Object> input,
        boolean requirePublishedFields
    ) {
        PageTemplateDefinition template = contentTemplate(column);
        Map<String, EditorFieldDefinition> allowed = template.editorSchema().extensionFields().stream()
            .filter(EditorFieldDefinition::enabled)
            .collect(Collectors.toMap(
                EditorFieldDefinition::fieldCode,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new
            ));
        Map<String, Object> source = input == null ? Map.of() : input;
        source.keySet().stream()
            .filter(key -> !allowed.containsKey(key))
            .findFirst()
            .ifPresent(key -> {
                throw badRequest("extensionData 包含未定义字段：" + key);
            });

        Map<String, Object> normalized = new LinkedHashMap<>();
        allowed.forEach((code, field) -> {
            Object value = source.containsKey(code) ? source.get(code) : field.defaultValue();
            Object checked = validateValue(field, value, "extensionData." + code);
            if (requirePublishedFields && field.required() && isEmpty(checked)) {
                throw badRequest("extensionData." + code + " 不能为空");
            }
            if (!isEmpty(checked)) {
                normalized.put(code, checked);
            }
        });
        return Map.copyOf(normalized);
    }

    public void validatePublishedContent(CmsColumn column, CmsContent content, LocalDateTime publishAt) {
        PageTemplateDefinition template = contentTemplate(column);
        for (EditorFieldDefinition field : template.editorSchema().contentFields()) {
            if (!field.enabled() || !field.required()) {
                continue;
            }
            Object value = commonValue(field.fieldCode(), content, publishAt);
            if (isEmpty(value)) {
                throw badRequest(field.fieldName() + "不能为空，不能发布");
            }
        }
        validateExtensionData(column, content.extensionData(), true);
    }

    public PageTemplateDefinition contentTemplate(CmsColumn column) {
        PageTemplateKey key = column.detailTemplateKey() == null
            ? column.templateKey()
            : column.detailTemplateKey();
        if (key == null) {
            throw badRequest("当前栏目不支持内容维护");
        }
        PageTemplateDefinition template = pageTemplateRegistry.findByKey(key)
            .orElseThrow(() -> badRequest("页面模板不存在：" + key));
        if (template.editorSchema().contentFields().isEmpty()) {
            throw badRequest("当前栏目不支持内容维护");
        }
        return template;
    }

    private Object commonValue(String code, CmsContent content, LocalDateTime publishAt) {
        return switch (code) {
            case "title" -> content.title();
            case "subtitle" -> content.subtitle();
            case "columnId" -> content.columnId();
            case "summary" -> content.summary();
            case "coverUrl" -> content.coverUrl();
            case "author" -> content.author();
            case "source" -> content.source();
            case "contentHtml" -> content.contentHtml();
            case "publishAt" -> publishAt;
            case "attachments" -> content.attachments();
            case "topFlag" -> content.topFlag();
            case "recommendFlag" -> content.recommendFlag();
            case "sortNo" -> content.sortNo();
            case "status" -> content.status();
            case "seoTitle" -> content.seoTitle();
            case "seoKeywords" -> content.seoKeywords();
            case "seoDescription" -> content.seoDescription();
            default -> null;
        };
    }

    private Object validateValue(EditorFieldDefinition field, Object value, String path) {
        if (value == null) {
            return null;
        }
        EditorFieldType type = field.fieldType();
        if (isStringType(type)) {
            if (!(value instanceof String text)) {
                throw badRequest(path + " 必须是字符串");
            }
            String normalized = StringUtils.hasText(text) ? text.trim() : null;
            if (normalized == null) {
                return null;
            }
            validateText(normalized, field.validationRule(), path);
            if (type == EditorFieldType.RICH_TEXT && (normalized.contains("<") || normalized.contains(">"))) {
                throw badRequest(path + " 不能包含 HTML");
            }
            if (type == EditorFieldType.LINK) {
                validateLink(normalized, path);
            }
            validateChoice(field, normalized, path);
            return normalized;
        }
        return switch (type) {
            case NUMBER -> validateNumber(value, field.validationRule(), path);
            case SWITCH -> requireType(value, Boolean.class, path, "布尔值");
            case IMAGE_LIST, FILE_LIST, CHECKBOX -> validateList(value, field, path);
            case COLUMN_SELECT, CONTENT_SELECT -> validateIdentifier(value, path);
            case DATE -> validateDate(value, path);
            case DATETIME -> validateDateTime(value, path);
            default -> throw badRequest(path + " 使用了不支持的字段类型：" + type);
        };
    }

    private boolean isStringType(EditorFieldType type) {
        return switch (type) {
            case TEXT, TEXTAREA, RICH_TEXT, SELECT, RADIO, IMAGE, FILE, LINK -> true;
            default -> false;
        };
    }

    private void validateText(String value, FieldValidationRule rule, String path) {
        if (rule.minLength() != null && value.length() < rule.minLength()) {
            throw badRequest(path + " 长度不能小于 " + rule.minLength());
        }
        if (rule.maxLength() != null && value.length() > rule.maxLength()) {
            throw badRequest(path + " 长度不能超过 " + rule.maxLength());
        }
        if (rule.pattern() != null && !value.matches(rule.pattern())) {
            throw badRequest(path + " 格式不正确");
        }
    }

    private Object validateNumber(Object value, FieldValidationRule rule, String path) {
        if (!(value instanceof Number number)) {
            throw badRequest(path + " 必须是数字");
        }
        long result = number.longValue();
        if (number.doubleValue() != result) {
            throw badRequest(path + " 必须是整数");
        }
        if (rule.minValue() != null && result < rule.minValue()) {
            throw badRequest(path + " 不能小于 " + rule.minValue());
        }
        if (rule.maxValue() != null && result > rule.maxValue()) {
            throw badRequest(path + " 不能大于 " + rule.maxValue());
        }
        return result;
    }

    private Object validateList(Object value, EditorFieldDefinition field, String path) {
        if (!(value instanceof List<?> list)) {
            throw badRequest(path + " 必须是数组");
        }
        List<String> normalized = list.stream().map(item -> {
            if (!(item instanceof String text) || !StringUtils.hasText(text)) {
                throw badRequest(path + " 只能包含非空字符串");
            }
            return text.trim();
        }).toList();
        Set<String> allowedValues = field.options().stream()
            .map(EditorFieldOption::value)
            .collect(Collectors.toSet());
        if (!allowedValues.isEmpty() && normalized.stream().anyMatch(item -> !allowedValues.contains(item))) {
            throw badRequest(path + " 包含非法选项");
        }
        return normalized;
    }

    private Object validateIdentifier(Object value, String path) {
        if (!(value instanceof Number number)
            || number.longValue() <= 0
            || number.doubleValue() != number.longValue()) {
            throw badRequest(path + " 必须是大于 0 的 ID");
        }
        return number.longValue();
    }

    private Object validateDate(Object value, String path) {
        if (value instanceof LocalDate) {
            return value;
        }
        if (value instanceof String text) {
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException ex) {
                throw badRequest(path + " 必须是 ISO 日期");
            }
        }
        throw badRequest(path + " 必须是日期");
    }

    private Object validateDateTime(Object value, String path) {
        if (value instanceof LocalDateTime) {
            return value;
        }
        if (value instanceof String text) {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException ex) {
                throw badRequest(path + " 必须是 ISO 日期时间");
            }
        }
        throw badRequest(path + " 必须是日期时间");
    }

    private void validateChoice(EditorFieldDefinition field, String value, String path) {
        Set<String> allowedValues = field.options().stream()
            .map(EditorFieldOption::value)
            .collect(Collectors.toSet());
        if (!allowedValues.isEmpty() && !allowedValues.contains(value)) {
            throw badRequest(path + " 不是允许的选项");
        }
    }

    private void validateLink(String value, String path) {
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
                throw badRequest(path + " 必须是有效的 HTTP 或 HTTPS 地址");
            }
        } catch (IllegalArgumentException ex) {
            throw badRequest(path + " 必须是有效的 HTTP 或 HTTPS 地址");
        }
    }

    private <T> T requireType(Object value, Class<T> type, String path, String typeName) {
        if (!type.isInstance(value)) {
            throw badRequest(path + " 必须是" + typeName);
        }
        return type.cast(value);
    }

    private boolean isEmpty(Object value) {
        return value == null
            || value instanceof String text && !StringUtils.hasText(text)
            || value instanceof List<?> list && list.isEmpty();
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }
}
