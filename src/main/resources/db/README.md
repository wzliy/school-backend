# 数据库脚本说明

## 新环境初始化

在空的 MySQL 8 数据库中执行：

```bash
mysql --default-character-set=utf8mb4 -u <user> -p <database> < src/main/resources/db/init.sql
```

`init.sql` 包含当前完整表结构、固定模板编码、初始管理员、权限、栏目、站点配置和默认页面区块。脚本不会创建数据库，执行前必须先选择目标数据库。

## 已有环境升级

已执行旧版 `init.sql` 的环境按文件名日期顺序执行 `migration` 目录中的脚本：

```bash
mysql --default-character-set=utf8mb4 -u <user> -p <database> \
  < src/main/resources/db/migration/20260717_page_template_schema.sql
```

本项目当前未引入 Flyway 或 Liquibase，增量脚本由部署人员手工执行并记录。生产执行前必须备份数据库；MySQL DDL 会隐式提交，不能依赖外层事务整体回滚。

## 本次升级验收

```sql
SHOW COLUMNS FROM cms_column;
SHOW COLUMNS FROM cms_content;
SHOW COLUMNS FROM cms_banner;
SHOW CREATE TABLE cms_page_section;

SELECT id, site_type, column_code, template_key, detail_template_key, template_config
FROM cms_column
WHERE deleted = 0
ORDER BY site_type, sort_no, id;

SELECT site_type, page_code, section_code, section_type, data_source_column_id,
       display_count, display_style, sort_no, enabled
FROM cms_page_section
WHERE deleted = 0
ORDER BY site_type, page_code, sort_no, id;
```

预期结果：历史 `page/list` 模板键已转换为固定大写编码，文章列表栏目默认详情模板为 `ARTICLE_DETAIL`，主站首页有 6 个默认区块，招生就业专题首页有 7 个默认区块。
