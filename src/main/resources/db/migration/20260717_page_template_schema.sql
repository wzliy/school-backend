-- 高校官网 CMS 固定页面模板数据库增量
-- 适用版本：MySQL 8.x
-- 执行前提：已执行 db/init.sql 的旧版本，并已 USE 目标数据库。
-- 注意：MySQL DDL 会隐式提交，生产执行前应完成备份并在测试环境演练。

SET NAMES utf8mb4;
SET @schema_name = DATABASE();

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_column' AND COLUMN_NAME = 'detail_template_key'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_column` ADD COLUMN `detail_template_key` VARCHAR(64) DEFAULT NULL COMMENT ''内容详情页模板编码'' AFTER `template_key`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_column' AND COLUMN_NAME = 'template_config'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_column` ADD COLUMN `template_config` JSON DEFAULT NULL COMMENT ''按 page/detail 分组的受控模板配置'' AFTER `detail_template_key`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_content' AND COLUMN_NAME = 'extension_data'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_content` ADD COLUMN `extension_data` JSON DEFAULT NULL COMMENT ''模板白名单约束的扩展数据'' AFTER `seo_description`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_banner' AND COLUMN_NAME = 'subtitle'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_banner` ADD COLUMN `subtitle` VARCHAR(255) DEFAULT NULL COMMENT ''副标题'' AFTER `title`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_banner' AND COLUMN_NAME = 'mobile_image_url'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_banner` ADD COLUMN `mobile_image_url` VARCHAR(512) DEFAULT NULL COMMENT ''手机端图片地址'' AFTER `image_url`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_banner' AND COLUMN_NAME = 'link_type'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_banner` ADD COLUMN `link_type` VARCHAR(16) NOT NULL DEFAULT ''NONE'' COMMENT ''跳转类型：NONE、CONTENT、COLUMN、EXTERNAL'' AFTER `mobile_image_url`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_banner' AND COLUMN_NAME = 'link_ref_id'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_banner` ADD COLUMN `link_ref_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''内部内容或栏目引用 ID'' AFTER `link_type`'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

SET @ddl = IF(
  EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'cms_banner' AND INDEX_NAME = 'idx_cms_banner_link_ref'
  ),
  'SELECT 1',
  'ALTER TABLE `cms_banner` ADD INDEX `idx_cms_banner_link_ref` (`link_type`, `link_ref_id`, `deleted`)'
);
PREPARE ddl_statement FROM @ddl;
EXECUTE ddl_statement;
DEALLOCATE PREPARE ddl_statement;

CREATE TABLE IF NOT EXISTS cms_page_section (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  site_type VARCHAR(32) NOT NULL COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE',
  page_code VARCHAR(64) NOT NULL COMMENT '页面编码：HOME、RECRUIT_HOME',
  section_code VARCHAR(64) NOT NULL COMMENT '页面内稳定区块编码',
  section_name VARCHAR(128) NOT NULL COMMENT '区块名称',
  section_type VARCHAR(32) NOT NULL COMMENT '区块类型：HERO_BANNER、CONTENT_FEED、QUICK_LINKS、IMAGE_GALLERY、FRIEND_LINKS、CONTACT_INFO',
  data_source_column_id BIGINT UNSIGNED DEFAULT NULL COMMENT '同站点数据源栏目 ID',
  display_count INT UNSIGNED DEFAULT NULL COMMENT '展示数量',
  display_style VARCHAR(64) DEFAULT NULL COMMENT '预定义展示样式',
  config_json JSON DEFAULT NULL COMMENT '区块类型白名单约束的配置',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cms_page_section_site_page_code_deleted (site_type, page_code, section_code, deleted),
  KEY idx_cms_page_section_page_enabled (site_type, page_code, enabled, deleted, sort_no),
  KEY idx_cms_page_section_data_source (data_source_column_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='固定模板页面区块表';

UPDATE cms_column
SET template_key = CASE
  WHEN LOWER(template_key) = 'page' THEN 'SINGLE_PAGE'
  WHEN LOWER(template_key) = 'list' AND site_type = 'MAIN_SITE' AND column_code = 'organization' THEN 'ORGANIZATION'
  WHEN LOWER(template_key) = 'list' AND site_type = 'MAIN_SITE' AND column_code = 'service' THEN 'SERVICE_DIRECTORY'
  WHEN LOWER(template_key) = 'list' THEN 'ARTICLE_LIST'
  WHEN UPPER(template_key) IN ('HOME', 'ARTICLE_LIST', 'ARTICLE_DETAIL', 'SINGLE_PAGE', 'ORGANIZATION', 'SERVICE_DIRECTORY', 'RECRUIT_HOME', 'SEARCH_RESULT') THEN UPPER(template_key)
  ELSE template_key
END
WHERE template_key IS NOT NULL;

UPDATE cms_column
SET detail_template_key = 'ARTICLE_DETAIL'
WHERE template_key = 'ARTICLE_LIST'
  AND detail_template_key IS NULL;

UPDATE cms_column
SET template_config = CASE template_key
  WHEN 'ARTICLE_LIST' THEN CAST('{"page":{"listStyle":"IMAGE_TEXT","pageSize":10,"showCover":true,"showSummary":true,"showPublishAt":true,"showViewCount":false,"defaultSort":"PUBLISH_AT_DESC","emptyText":"暂无相关内容"},"detail":{"showAuthor":true,"showSource":true,"showPublishAt":true,"showViewCount":true,"showSiblingNavigation":true,"showAttachments":true,"showRelated":false,"relatedCount":4,"shareEnabled":false}}' AS JSON)
  WHEN 'SINGLE_PAGE' THEN CAST('{"page":{}}' AS JSON)
  WHEN 'ORGANIZATION' THEN CAST('{"page":{"displayStyle":"GROUPED"}}' AS JSON)
  WHEN 'SERVICE_DIRECTORY' THEN CAST('{"page":{"displayStyle":"ICON_GRID"}}' AS JSON)
  ELSE template_config
END
WHERE template_config IS NULL
  AND template_key IN ('ARTICLE_LIST', 'SINGLE_PAGE', 'ORGANIZATION', 'SERVICE_DIRECTORY');

UPDATE cms_banner
SET link_type = 'EXTERNAL'
WHERE link_type = 'NONE'
  AND link_ref_id IS NULL
  AND link_url IS NOT NULL
  AND TRIM(link_url) <> '';

INSERT INTO cms_page_section (
  site_type, page_code, section_code, section_name, section_type, data_source_column_id, display_count, display_style, config_json, sort_no, enabled, created_by, updated_by, deleted
) VALUES
  ('MAIN_SITE', 'HOME', 'HERO', '首页轮播', 'HERO_BANNER', NULL, NULL, 'FULL_WIDTH', '{"bannerPosition":"HOME"}', 10, 1, 1, 1, 0),
  ('MAIN_SITE', 'HOME', 'SCHOOL_NEWS', '学校新闻', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'MAIN_SITE' AND column_code = 'news' AND deleted = 0 LIMIT 1), 6, 'IMAGE_TEXT', NULL, 20, 1, 1, 1, 0),
  ('MAIN_SITE', 'HOME', 'NOTICE', '通知公告', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'MAIN_SITE' AND column_code = 'notice' AND deleted = 0 LIMIT 1), 8, 'TEXT_LIST', NULL, 30, 1, 1, 1, 0),
  ('MAIN_SITE', 'HOME', 'QUICK_LINKS', '快捷入口', 'QUICK_LINKS', NULL, NULL, 'ICON_GRID', NULL, 40, 1, 1, 1, 0),
  ('MAIN_SITE', 'HOME', 'CAMPUS_GALLERY', '校园风采', 'IMAGE_GALLERY', NULL, 8, 'GRID', NULL, 50, 1, 1, 1, 0),
  ('MAIN_SITE', 'HOME', 'FRIEND_LINKS', '友情链接', 'FRIEND_LINKS', NULL, NULL, 'TEXT_LINKS', NULL, 60, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'HERO', '专题主视觉', 'HERO_BANNER', NULL, NULL, 'FULL_WIDTH', '{"bannerPosition":"RECRUIT_HOME"}', 10, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'ADMISSION_NEWS', '招生动态', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'RECRUIT_SITE' AND column_code = 'admission' AND deleted = 0 LIMIT 1), 6, 'IMAGE_TEXT', NULL, 20, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'EMPLOYMENT_NEWS', '就业信息', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'RECRUIT_SITE' AND column_code = 'employment' AND deleted = 0 LIMIT 1), 6, 'TEXT_LIST', NULL, 30, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'SCHOOL_ENTERPRISE', '校企合作', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'RECRUIT_SITE' AND column_code = 'school-enterprise' AND deleted = 0 LIMIT 1), 6, 'IMAGE_TEXT', NULL, 40, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'RECRUIT_POLICY', '政策公告', 'CONTENT_FEED', (SELECT id FROM cms_column WHERE site_type = 'RECRUIT_SITE' AND column_code = 'recruit-policy' AND deleted = 0 LIMIT 1), 8, 'TEXT_LIST', NULL, 50, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'QUICK_LINKS', '专题快捷入口', 'QUICK_LINKS', NULL, NULL, 'ICON_GRID', NULL, 60, 1, 1, 1, 0),
  ('RECRUIT_SITE', 'RECRUIT_HOME', 'CONTACT', '联系我们', 'CONTACT_INFO', NULL, NULL, 'DEFAULT', NULL, 70, 1, 1, 1, 0)
ON DUPLICATE KEY UPDATE
  section_name = VALUES(section_name),
  section_type = VALUES(section_type),
  data_source_column_id = COALESCE(cms_page_section.data_source_column_id, VALUES(data_source_column_id)),
  display_count = COALESCE(cms_page_section.display_count, VALUES(display_count)),
  display_style = COALESCE(cms_page_section.display_style, VALUES(display_style)),
  config_json = COALESCE(cms_page_section.config_json, VALUES(config_json)),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

SET @ddl = NULL;
SET @schema_name = NULL;
