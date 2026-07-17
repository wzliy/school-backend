package com.zlwang.school.modules.template;

import static com.zlwang.school.modules.template.model.ColumnType.LINK;
import static com.zlwang.school.modules.template.model.ColumnType.LIST;
import static com.zlwang.school.modules.template.model.ColumnType.PAGE;
import static com.zlwang.school.modules.template.model.ColumnType.SPECIAL;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_LIST;
import static com.zlwang.school.modules.template.model.PageTemplateKey.HOME;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ORGANIZATION;
import static com.zlwang.school.modules.template.model.PageTemplateKey.RECRUIT_HOME;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SEARCH_RESULT;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SERVICE_DIRECTORY;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SINGLE_PAGE;
import static com.zlwang.school.modules.template.model.SiteType.MAIN_SITE;
import static com.zlwang.school.modules.template.model.SiteType.RECRUIT_SITE;
import static org.assertj.core.api.Assertions.assertThat;

import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.EditorSchema;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.service.PageTemplateRegistry;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageTemplateRegistryTests {

    private final PageTemplateRegistry registry = new PageTemplateRegistry();

    @Test
    void registersExactlyEightTemplatesInStableOrder() {
        assertThat(registry.findAll())
            .extracting(PageTemplateDefinition::templateKey)
            .containsExactly(
                HOME,
                ARTICLE_LIST,
                com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_DETAIL,
                SINGLE_PAGE,
                ORGANIZATION,
                SERVICE_DIRECTORY,
                RECRUIT_HOME,
                SEARCH_RESULT
            );
    }

    @Test
    void enforcesSiteAndColumnCompatibility() {
        assertThat(registry.isCompatible(HOME, MAIN_SITE, SPECIAL)).isTrue();
        assertThat(registry.isCompatible(HOME, RECRUIT_SITE, SPECIAL)).isFalse();
        assertThat(registry.isCompatible(RECRUIT_HOME, RECRUIT_SITE, SPECIAL)).isTrue();
        assertThat(registry.isCompatible(RECRUIT_HOME, MAIN_SITE, SPECIAL)).isFalse();
        assertThat(registry.isCompatible(ARTICLE_LIST, MAIN_SITE, LIST)).isTrue();
        assertThat(registry.isCompatible(ARTICLE_LIST, MAIN_SITE, LINK)).isFalse();
        assertThat(registry.isCompatible(SINGLE_PAGE, MAIN_SITE, PAGE)).isTrue();
        assertThat(registry.isCompatible(ORGANIZATION, RECRUIT_SITE, LIST)).isFalse();
        assertThat(registry.isCompatible(SEARCH_RESULT, MAIN_SITE, SPECIAL)).isFalse();
    }

    @Test
    void exposesStructuredAndUnambiguousEditorSchemas() {
        for (PageTemplateDefinition template : registry.findAll()) {
            EditorSchema schema = template.editorSchema();
            assertUniqueAndEnabled(template, "columnFields", schema.columnFields());
            assertUniqueAndEnabled(template, "contentFields", schema.contentFields());
            assertUniqueAndEnabled(template, "extensionFields", schema.extensionFields());
            assertUniqueAndEnabled(template, "pageFields", schema.pageFields());
        }

        PageTemplateDefinition organization = registry.findByKey(ORGANIZATION).orElseThrow();
        assertThat(organization.editorSchema().extensionFields())
            .extracting(EditorFieldDefinition::fieldCode)
            .contains("logoUrl", "leader", "websiteUrl");
        assertThat(registry.findByKey(ARTICLE_LIST).orElseThrow().defaultDetailTemplateKey())
            .isEqualTo(com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_DETAIL);
    }

    private void assertUniqueAndEnabled(
        PageTemplateDefinition template,
        String group,
        List<EditorFieldDefinition> fields
    ) {
        HashSet<String> codes = new HashSet<>();
        assertThat(fields).allSatisfy(field -> {
            assertThat(field.enabled()).isTrue();
            assertThat(field.sort()).isPositive();
            assertThat(codes.add(field.fieldCode()))
                .as("模板 %s 的 %s 中字段编码 %s 不应重复", template.templateKey(), group, field.fieldCode())
                .isTrue();
        });
    }
}
