package com.zlwang.school.infrastructure.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class DatabaseSchemaContractTests {

    private static final String INIT_SQL = readResource("/db/init.sql");
    private static final String MIGRATION_SQL = readResource(
        "/db/migration/20260717_page_template_schema.sql"
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void initializationContainsCompletePageTemplateSchema() {
        assertThat(INIT_SQL)
            .contains("detail_template_key VARCHAR(64)")
            .contains("template_config JSON")
            .contains("extension_data JSON")
            .contains("subtitle VARCHAR(255)")
            .contains("mobile_image_url VARCHAR(512)")
            .contains("link_type VARCHAR(16)")
            .contains("link_ref_id BIGINT UNSIGNED")
            .contains("idx_cms_banner_link_ref (link_type, link_ref_id, deleted)")
            .contains("CREATE TABLE IF NOT EXISTS cms_page_section")
            .contains("uk_cms_page_section_site_page_code_deleted")
            .doesNotContain(", 'page',", ", 'list',");

        assertThat(INIT_SQL)
            .contains("'SINGLE_PAGE'")
            .contains("'ARTICLE_LIST'")
            .contains("'ARTICLE_DETAIL'")
            .contains("'ORGANIZATION'")
            .contains("'SERVICE_DIRECTORY'");
    }

    @Test
    void migrationIsRepeatableAndMigratesLegacyTemplateKeys() {
        assertThat(occurrences(MIGRATION_SQL, "information_schema.COLUMNS")).isEqualTo(7);
        assertThat(occurrences(MIGRATION_SQL, "information_schema.STATISTICS")).isEqualTo(1);
        assertThat(occurrences(MIGRATION_SQL, "PREPARE ddl_statement FROM @ddl")).isEqualTo(8);
        assertThat(MIGRATION_SQL)
            .contains("CREATE TABLE IF NOT EXISTS cms_page_section")
            .contains("ON DUPLICATE KEY UPDATE")
            .contains("LOWER(template_key) = 'page'")
            .contains("LOWER(template_key) = 'list'")
            .contains("THEN 'SINGLE_PAGE'")
            .contains("THEN 'ARTICLE_LIST'")
            .contains("THEN 'ORGANIZATION'")
            .contains("THEN 'SERVICE_DIRECTORY'")
            .contains("SET detail_template_key = 'ARTICLE_DETAIL'")
            .contains("SET link_type = 'EXTERNAL'")
            .contains("WHERE template_config IS NULL");
        assertThat(MIGRATION_SQL.indexOf("COLUMN_NAME = 'link_ref_id'"))
            .isLessThan(MIGRATION_SQL.indexOf("INDEX_NAME = 'idx_cms_banner_link_ref'"));
    }

    @Test
    void initializationAndMigrationSharePageSectionContractAndSeeds() {
        assertThat(normalize(pageSectionTable(INIT_SQL)))
            .isEqualTo(normalize(pageSectionTable(MIGRATION_SQL)));
        assertThat(normalize(pageSectionSeeds(INIT_SQL)))
            .isEqualTo(normalize(pageSectionSeeds(MIGRATION_SQL)));
        assertThat(occurrences(pageSectionSeeds(INIT_SQL), "('MAIN_SITE', 'HOME'"))
            .isEqualTo(6);
        assertThat(occurrences(pageSectionSeeds(INIT_SQL), "('RECRUIT_SITE', 'RECRUIT_HOME'"))
            .isEqualTo(7);
    }

    @Test
    void embeddedJsonDefaultsAreValid() throws Exception {
        assertEmbeddedJsonIsValid(INIT_SQL, 6);
        assertEmbeddedJsonIsValid(MIGRATION_SQL, 6);
    }

    private static String pageSectionTable(String sql) {
        return between(
            sql,
            "CREATE TABLE IF NOT EXISTS cms_page_section (",
            "COMMENT='固定模板页面区块表';"
        );
    }

    private static String pageSectionSeeds(String sql) {
        return between(
            sql,
            "INSERT INTO cms_page_section (",
            "  deleted = VALUES(deleted);"
        );
    }

    private static String between(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex);
        assertThat(startIndex).as("SQL 起始标记应存在: %s", start).isNotNegative();
        assertThat(endIndex).as("SQL 结束标记应存在: %s", end).isNotNegative();
        return text.substring(startIndex, endIndex + end.length());
    }

    private static long occurrences(String text, String literal) {
        Matcher matcher = Pattern.compile(Pattern.quote(literal)).matcher(text);
        long count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static void assertEmbeddedJsonIsValid(String sql, int expectedCount) throws Exception {
        Matcher matcher = Pattern.compile("'(\\{[^']*})'").matcher(sql);
        int count = 0;
        while (matcher.find()) {
            OBJECT_MAPPER.readTree(matcher.group(1));
            count++;
        }
        assertThat(count).isEqualTo(expectedCount);
    }

    private static String readResource(String path) {
        try (InputStream input = DatabaseSchemaContractTests.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("找不到测试资源: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("读取测试资源失败: " + path, ex);
        }
    }
}
