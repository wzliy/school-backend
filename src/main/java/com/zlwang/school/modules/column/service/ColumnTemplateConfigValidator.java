package com.zlwang.school.modules.column.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.EditorFieldType;
import com.zlwang.school.modules.template.model.FieldValidationRule;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.service.PageTemplateRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ColumnTemplateConfigValidator {

    private static final Set<String> ROOT_KEYS = Set.of("page", "detail");
    private static final Set<String> FIXED_COLUMN_FIELDS = Set.of("coverUrl");

    private final PageTemplateRegistry pageTemplateRegistry;

    public ColumnTemplateConfigValidator(PageTemplateRegistry pageTemplateRegistry) {
        this.pageTemplateRegistry = pageTemplateRegistry;
    }

    public Map<String, Object> validateAndApplyDefaults(
        PageTemplateKey pageTemplateKey,
        PageTemplateKey detailTemplateKey,
        Map<String, Object> input
    ) {
        Map<String, Object> source = input == null ? Map.of() : input;
        rejectUnknownKeys(source, ROOT_KEYS, "templateConfig");

        PageTemplateDefinition pageTemplate = requiredTemplate(pageTemplateKey);
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("page", validateGroup(
            asObject(source.get("page"), "templateConfig.page"),
            pageTemplate.editorSchema().columnFields(),
            "templateConfig.page"
        ));

        if (detailTemplateKey != null) {
            PageTemplateDefinition detailTemplate = requiredTemplate(detailTemplateKey);
            normalized.put("detail", validateGroup(
                asObject(source.get("detail"), "templateConfig.detail"),
                detailTemplate.editorSchema().columnFields(),
                "templateConfig.detail"
            ));
        } else if (!asObject(source.get("detail"), "templateConfig.detail").isEmpty()) {
            throw badRequest("当前栏目没有详情模板，不能配置 templateConfig.detail");
        }
        return Map.copyOf(normalized);
    }

    private Map<String, Object> validateGroup(
        Map<String, Object> source,
        List<EditorFieldDefinition> fields,
        String path
    ) {
        Map<String, EditorFieldDefinition> allowed = fields.stream()
            .filter(field -> !FIXED_COLUMN_FIELDS.contains(field.fieldCode()))
            .collect(java.util.stream.Collectors.toMap(
                EditorFieldDefinition::fieldCode,
                field -> field,
                (left, right) -> left,
                LinkedHashMap::new
            ));
        rejectUnknownKeys(source, allowed.keySet(), path);

        Map<String, Object> normalized = new LinkedHashMap<>();
        for (EditorFieldDefinition field : allowed.values()) {
            Object value = source.containsKey(field.fieldCode())
                ? source.get(field.fieldCode())
                : field.defaultValue();
            if (value == null) {
                if (field.required()) {
                    throw badRequest(path + "." + field.fieldCode() + " 不能为空");
                }
                continue;
            }
            validateValue(field, value, path + "." + field.fieldCode());
            normalized.put(field.fieldCode(), value);
        }
        return Map.copyOf(normalized);
    }

    private void validateValue(EditorFieldDefinition field, Object value, String path) {
        EditorFieldType type = field.fieldType();
        if (type == EditorFieldType.SWITCH && !(value instanceof Boolean)) {
            throw badRequest(path + " 必须是布尔值");
        }
        if (type == EditorFieldType.NUMBER && !(value instanceof Number)) {
            throw badRequest(path + " 必须是数字");
        }
        if (isStringType(type) && !(value instanceof String)) {
            throw badRequest(path + " 必须是字符串");
        }

        FieldValidationRule rule = field.validationRule();
        if (value instanceof String text) {
            if (rule.minLength() != null && text.length() < rule.minLength()) {
                throw badRequest(path + " 长度不能小于 " + rule.minLength());
            }
            if (rule.maxLength() != null && text.length() > rule.maxLength()) {
                throw badRequest(path + " 长度不能大于 " + rule.maxLength());
            }
            if (!rule.allowedValues().isEmpty() && !rule.allowedValues().contains(text)) {
                throw badRequest(path + " 不在允许选项中");
            }
        }
        if (value instanceof Number number) {
            long longValue = number.longValue();
            if (rule.minValue() != null && longValue < rule.minValue()) {
                throw badRequest(path + " 不能小于 " + rule.minValue());
            }
            if (rule.maxValue() != null && longValue > rule.maxValue()) {
                throw badRequest(path + " 不能大于 " + rule.maxValue());
            }
        }
    }

    private boolean isStringType(EditorFieldType type) {
        return switch (type) {
            case TEXT, TEXTAREA, SELECT, RADIO, IMAGE, LINK -> true;
            default -> false;
        };
    }

    private Map<String, Object> asObject(Object value, String path) {
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw badRequest(path + " 必须是对象");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> {
            if (!(key instanceof String stringKey) || !StringUtils.hasText(stringKey)) {
                throw badRequest(path + " 包含非法字段名");
            }
            result.put(stringKey, item);
        });
        return result;
    }

    private void rejectUnknownKeys(Map<String, ?> source, Set<String> allowed, String path) {
        source.keySet().stream()
            .filter(key -> !allowed.contains(key))
            .findFirst()
            .ifPresent(key -> {
                throw badRequest(path + " 包含未定义字段：" + key);
            });
    }

    private PageTemplateDefinition requiredTemplate(PageTemplateKey templateKey) {
        return pageTemplateRegistry.findByKey(templateKey)
            .orElseThrow(() -> badRequest("页面模板不存在：" + templateKey));
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }
}
