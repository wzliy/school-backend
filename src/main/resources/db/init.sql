-- 高校官网 CMS 系统 MySQL 8 初始化脚本
-- 默认管理员：admin / Admin@123456
-- 说明：脚本使用 CREATE TABLE IF NOT EXISTS 与固定主键初始化数据，便于开发环境重复执行。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  real_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '真实姓名',
  avatar_url VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录 IP',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_status_deleted (status, deleted),
  KEY idx_sys_user_phone_deleted (phone, deleted),
  KEY idx_sys_user_email_deleted (email, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code),
  KEY idx_sys_role_status_deleted (status, deleted),
  KEY idx_sys_role_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级权限 ID',
  permission_name VARCHAR(64) NOT NULL COMMENT '权限名称',
  permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
  permission_type VARCHAR(16) NOT NULL COMMENT '权限类型：MENU、BUTTON、API',
  route_path VARCHAR(255) DEFAULT NULL COMMENT '前端路由路径',
  component_path VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
  icon VARCHAR(64) DEFAULT NULL COMMENT '菜单图标',
  api_method VARCHAR(16) DEFAULT NULL COMMENT '接口方法',
  api_path VARCHAR(255) DEFAULT NULL COMMENT '接口路径',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0 否，1 是',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (permission_code),
  KEY idx_sys_permission_parent_deleted (parent_id, deleted),
  KEY idx_sys_permission_type_status (permission_type, status),
  KEY idx_sys_permission_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role_user_role_deleted (user_id, role_id, deleted),
  KEY idx_sys_user_role_role_id (role_id),
  KEY idx_sys_user_role_user_deleted (user_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission_role_perm_deleted (role_id, permission_id, deleted),
  KEY idx_sys_role_permission_permission_id (permission_id),
  KEY idx_sys_role_permission_role_deleted (role_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系表';

CREATE TABLE IF NOT EXISTS cms_column (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级栏目 ID',
  site_type VARCHAR(32) NOT NULL DEFAULT 'MAIN_SITE' COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE',
  column_name VARCHAR(128) NOT NULL COMMENT '栏目名称',
  column_code VARCHAR(128) NOT NULL COMMENT '栏目编码',
  column_type VARCHAR(32) NOT NULL COMMENT '栏目类型：PAGE、LIST、IMAGE、DOWNLOAD、LINK、SPECIAL',
  route_path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  external_url VARCHAR(512) DEFAULT NULL COMMENT '外链地址',
  template_key VARCHAR(64) DEFAULT NULL COMMENT '模板标识',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '栏目封面',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  nav_visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示在导航：0 否，1 是',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
  seo_title VARCHAR(255) DEFAULT NULL COMMENT 'SEO 标题',
  seo_keywords VARCHAR(512) DEFAULT NULL COMMENT 'SEO 关键词',
  seo_description VARCHAR(1024) DEFAULT NULL COMMENT 'SEO 描述',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cms_column_site_code_deleted (site_type, column_code, deleted),
  KEY idx_cms_column_parent_site_deleted (parent_id, site_type, deleted),
  KEY idx_cms_column_site_nav_enabled (site_type, nav_visible, enabled, deleted),
  KEY idx_cms_column_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='栏目表';

CREATE TABLE IF NOT EXISTS cms_content (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  column_id BIGINT UNSIGNED NOT NULL COMMENT '所属栏目 ID',
  site_type VARCHAR(32) NOT NULL DEFAULT 'MAIN_SITE' COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  subtitle VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  summary VARCHAR(1024) DEFAULT NULL COMMENT '摘要',
  content_html LONGTEXT COMMENT '正文 HTML',
  cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  source VARCHAR(128) DEFAULT NULL COMMENT '来源',
  author VARCHAR(64) DEFAULT NULL COMMENT '作者',
  publish_at DATETIME DEFAULT NULL COMMENT '发布时间',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、PUBLISHED、OFFLINE、DELETED',
  top_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0 否，1 是',
  recommend_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐：0 否，1 是',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  seo_title VARCHAR(255) DEFAULT NULL COMMENT 'SEO 标题',
  seo_keywords VARCHAR(512) DEFAULT NULL COMMENT 'SEO 关键词',
  seo_description VARCHAR(1024) DEFAULT NULL COMMENT 'SEO 描述',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_cms_content_column_status_publish (column_id, status, publish_at, deleted),
  KEY idx_cms_content_site_status_publish (site_type, status, publish_at, deleted),
  KEY idx_cms_content_title (title),
  KEY idx_cms_content_top_recommend (top_flag, recommend_flag, deleted),
  KEY idx_cms_content_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容表';

CREATE TABLE IF NOT EXISTS cms_content_attachment (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容 ID',
  media_id BIGINT UNSIGNED DEFAULT NULL COMMENT '媒体库文件 ID',
  file_name VARCHAR(255) NOT NULL COMMENT '附件名称',
  file_url VARCHAR(512) NOT NULL COMMENT '附件访问地址',
  file_size BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  file_type VARCHAR(32) NOT NULL DEFAULT 'OTHER' COMMENT '文件类型：IMAGE、DOCUMENT、VIDEO、OTHER',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_cms_content_attachment_content_deleted (content_id, deleted),
  KEY idx_cms_content_attachment_media_id (media_id),
  KEY idx_cms_content_attachment_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容附件表';

CREATE TABLE IF NOT EXISTS cms_banner (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  site_type VARCHAR(32) NOT NULL DEFAULT 'MAIN_SITE' COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE',
  position VARCHAR(64) NOT NULL COMMENT '展示位置：HOME、RECRUIT_HOME、COLUMN',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  image_url VARCHAR(512) NOT NULL COMMENT '图片地址',
  link_url VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
  link_target VARCHAR(16) NOT NULL DEFAULT '_self' COMMENT '打开方式：_self、_blank',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
  start_time DATETIME DEFAULT NULL COMMENT '展示开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '展示结束时间',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_cms_banner_site_position_enabled (site_type, position, enabled, deleted),
  KEY idx_cms_banner_time_range (start_time, end_time),
  KEY idx_cms_banner_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Banner 表';

CREATE TABLE IF NOT EXISTS cms_media (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  storage_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL、MINIO',
  file_type VARCHAR(32) NOT NULL DEFAULT 'OTHER' COMMENT '文件类型：IMAGE、DOCUMENT、VIDEO、OTHER',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  extension VARCHAR(32) DEFAULT NULL COMMENT '文件扩展名',
  mime_type VARCHAR(128) DEFAULT NULL COMMENT 'MIME 类型',
  file_size BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  file_path VARCHAR(512) NOT NULL COMMENT '存储路径',
  access_url VARCHAR(512) NOT NULL COMMENT '访问地址',
  uploader_id BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人 ID',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_cms_media_file_type_deleted (file_type, deleted),
  KEY idx_cms_media_storage_type (storage_type),
  KEY idx_cms_media_uploader_created (uploader_id, created_at),
  KEY idx_cms_media_original_name (original_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='媒体文件表';

CREATE TABLE IF NOT EXISTS cms_site_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  site_type VARCHAR(32) NOT NULL DEFAULT 'MAIN_SITE' COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE、GLOBAL',
  config_key VARCHAR(128) NOT NULL COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  config_type VARCHAR(32) NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING、NUMBER、BOOLEAN、JSON、IMAGE',
  description VARCHAR(512) DEFAULT NULL COMMENT '配置说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cms_site_config_site_key_deleted (site_type, config_key, deleted),
  KEY idx_cms_site_config_site_deleted (site_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点配置表';

CREATE TABLE IF NOT EXISTS cms_friend_link (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  site_type VARCHAR(32) NOT NULL DEFAULT 'MAIN_SITE' COMMENT '站点类型：MAIN_SITE、RECRUIT_SITE、GLOBAL',
  name VARCHAR(128) NOT NULL COMMENT '链接名称',
  link_url VARCHAR(512) NOT NULL COMMENT '链接地址',
  logo_url VARCHAR(512) DEFAULT NULL COMMENT 'Logo 地址',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0 否，1 是',
  remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_cms_friend_link_site_enabled (site_type, enabled, deleted),
  KEY idx_cms_friend_link_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='友情链接表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户 ID',
  username VARCHAR(64) DEFAULT NULL COMMENT '操作账号',
  module_name VARCHAR(64) NOT NULL COMMENT '模块名称',
  operation_type VARCHAR(32) NOT NULL COMMENT '操作类型',
  request_method VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求 URI',
  request_ip VARCHAR(64) DEFAULT NULL COMMENT '请求 IP',
  request_params TEXT COMMENT '请求参数摘要',
  result_status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '结果：SUCCESS、FAIL',
  error_message TEXT COMMENT '错误信息',
  cost_ms BIGINT UNSIGNED DEFAULT NULL COMMENT '耗时，单位毫秒',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_sys_operation_log_user_created (user_id, created_at),
  KEY idx_sys_operation_log_module_created (module_name, created_at),
  KEY idx_sys_operation_log_result_created (result_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS sys_login_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '用户 ID',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  login_ip VARCHAR(64) DEFAULT NULL COMMENT '登录 IP',
  user_agent VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
  login_status VARCHAR(16) NOT NULL COMMENT '登录结果：SUCCESS、FAIL',
  failure_reason VARCHAR(512) DEFAULT NULL COMMENT '失败原因',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (id),
  KEY idx_sys_login_log_username_created (username, created_at),
  KEY idx_sys_login_log_user_created (user_id, created_at),
  KEY idx_sys_login_log_status_created (login_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

INSERT INTO sys_user (
  id, username, password, real_name, email, status, remark, created_by, updated_by, deleted
) VALUES (
  1,
  'admin',
  '$2y$10$AT7CX..4P1ofYP8xM/j5cOXEDIvskr6yCAtYz5WHIXBm97Luq5IWa',
  '超级管理员',
  'admin@example.com',
  1,
  '系统初始化管理员，默认密码 Admin@123456，上线前请立即修改',
  1,
  1,
  0
) ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  email = VALUES(email),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO sys_role (
  id, role_name, role_code, status, sort_no, remark, created_by, updated_by, deleted
) VALUES
  (1, '超级管理员', 'SUPER_ADMIN', 1, 1, '拥有系统全部权限', 1, 1, 0),
  (2, '网站管理员', 'SITE_ADMIN', 1, 2, '管理栏目、内容、Banner、媒体库和站点配置', 1, 1, 0),
  (3, '内容编辑', 'CONTENT_EDITOR', 1, 3, '维护指定栏目内容', 1, 1, 0),
  (4, '内容审核员', 'CONTENT_AUDITOR', 1, 4, '预留审核权限，首版不启用审核流', 1, 1, 0),
  (5, '招生就业管理员', 'RECRUIT_ADMIN', 1, 5, '维护招生就业专题站内容', 1, 1, 0)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  status = VALUES(status),
  sort_no = VALUES(sort_no),
  remark = VALUES(remark),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO sys_permission (
  id, parent_id, permission_name, permission_code, permission_type, route_path, component_path, icon, api_method, api_path, sort_no, visible, status, remark, created_by, updated_by, deleted
) VALUES
  (1, 0, '系统管理', 'system', 'MENU', '/system', 'Layout', 'Setting', NULL, NULL, 10, 1, 1, NULL, 1, 1, 0),
  (2, 1, '用户管理', 'system:user', 'MENU', '/system/users', 'system/user/index', 'User', NULL, NULL, 11, 1, 1, NULL, 1, 1, 0),
  (3, 1, '角色管理', 'system:role', 'MENU', '/system/roles', 'system/role/index', 'UserRoundCog', NULL, NULL, 12, 1, 1, NULL, 1, 1, 0),
  (4, 1, '权限管理', 'system:permission', 'MENU', '/system/permissions', 'system/permission/index', 'ShieldCheck', NULL, NULL, 13, 1, 1, NULL, 1, 1, 0),
  (5, 0, 'CMS 管理', 'cms', 'MENU', '/cms', 'Layout', 'Newspaper', NULL, NULL, 20, 1, 1, NULL, 1, 1, 0),
  (6, 5, '栏目管理', 'cms:column', 'MENU', '/cms/columns', 'cms/column/index', 'PanelTop', NULL, NULL, 21, 1, 1, NULL, 1, 1, 0),
  (7, 5, '内容管理', 'cms:content', 'MENU', '/cms/contents', 'cms/content/index', 'FileText', NULL, NULL, 22, 1, 1, NULL, 1, 1, 0),
  (8, 5, 'Banner 管理', 'cms:banner', 'MENU', '/cms/banners', 'cms/banner/index', 'Images', NULL, NULL, 23, 1, 1, NULL, 1, 1, 0),
  (9, 5, '媒体库管理', 'cms:media', 'MENU', '/cms/media', 'cms/media/index', 'FolderOpen', NULL, NULL, 24, 1, 1, NULL, 1, 1, 0),
  (10, 5, '友情链接管理', 'cms:friend-link', 'MENU', '/cms/friend-links', 'cms/friend-link/index', 'Link', NULL, NULL, 25, 1, 1, NULL, 1, 1, 0),
  (11, 5, '站点配置', 'cms:site-config', 'MENU', '/cms/site-config', 'cms/site-config/index', 'SlidersHorizontal', NULL, NULL, 26, 1, 1, NULL, 1, 1, 0),
  (12, 0, '日志管理', 'log', 'MENU', '/logs', 'Layout', 'ClipboardList', NULL, NULL, 30, 1, 1, NULL, 1, 1, 0),
  (13, 12, '操作日志', 'log:operation', 'MENU', '/logs/operations', 'log/operation/index', 'ListChecks', NULL, NULL, 31, 1, 1, NULL, 1, 1, 0),
  (14, 12, '登录日志', 'log:login', 'MENU', '/logs/login', 'log/login/index', 'LogIn', NULL, NULL, 32, 1, 1, NULL, 1, 1, 0),
  (101, 2, '用户新增', 'system:user:create', 'BUTTON', NULL, NULL, NULL, 'POST', '/api/admin/users', 101, 0, 1, NULL, 1, 1, 0),
  (102, 2, '用户编辑', 'system:user:update', 'BUTTON', NULL, NULL, NULL, 'PUT', '/api/admin/users/{id}', 102, 0, 1, NULL, 1, 1, 0),
  (103, 2, '用户删除', 'system:user:delete', 'BUTTON', NULL, NULL, NULL, 'DELETE', '/api/admin/users/{id}', 103, 0, 1, NULL, 1, 1, 0),
  (104, 3, '角色维护', 'system:role:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/roles/**', 104, 0, 1, NULL, 1, 1, 0),
  (105, 4, '权限维护', 'system:permission:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/permissions/**', 105, 0, 1, NULL, 1, 1, 0),
  (201, 6, '栏目维护', 'cms:column:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/columns/**', 201, 0, 1, NULL, 1, 1, 0),
  (202, 7, '内容维护', 'cms:content:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/contents/**', 202, 0, 1, NULL, 1, 1, 0),
  (203, 8, 'Banner 维护', 'cms:banner:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/banners/**', 203, 0, 1, NULL, 1, 1, 0),
  (204, 9, '媒体库维护', 'cms:media:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/media/**', 204, 0, 1, NULL, 1, 1, 0),
  (205, 10, '友情链接维护', 'cms:friend-link:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/friend-links/**', 205, 0, 1, NULL, 1, 1, 0),
  (206, 11, '站点配置维护', 'cms:site-config:manage', 'BUTTON', NULL, NULL, NULL, '*', '/api/admin/site-config/**', 206, 0, 1, NULL, 1, 1, 0)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  route_path = VALUES(route_path),
  component_path = VALUES(component_path),
  icon = VALUES(icon),
  api_method = VALUES(api_method),
  api_path = VALUES(api_path),
  sort_no = VALUES(sort_no),
  visible = VALUES(visible),
  status = VALUES(status),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO sys_user_role (
  user_id, role_id, created_by, updated_by, deleted
) VALUES
  (1, 1, 1, 1, 0)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO sys_role_permission (
  role_id, permission_id, created_by, updated_by, deleted
)
SELECT
  1,
  p.id,
  1,
  1,
  0
FROM sys_permission p
WHERE p.deleted = 0
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  permission_id = VALUES(permission_id),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO cms_column (
  id, parent_id, site_type, column_name, column_code, column_type, route_path, external_url, template_key, sort_no, nav_visible, enabled, seo_title, seo_keywords, seo_description, remark, created_by, updated_by, deleted
) VALUES
  (100, 0, 'MAIN_SITE', '学校概况', 'about', 'PAGE', '/about', NULL, 'page', 10, 1, 1, '学校概况', '学校概况,高校官网', '展示学校简介、校史沿革、校园风貌等内容', NULL, 1, 1, 0),
  (101, 0, 'MAIN_SITE', '新闻中心', 'news', 'LIST', '/news', NULL, 'list', 20, 1, 1, '新闻中心', '新闻,动态,高校官网', '展示学校新闻动态', NULL, 1, 1, 0),
  (102, 0, 'MAIN_SITE', '通知公告', 'notice', 'LIST', '/notice', NULL, 'list', 30, 1, 1, '通知公告', '通知,公告', '展示学校通知公告', NULL, 1, 1, 0),
  (103, 0, 'MAIN_SITE', '机构设置', 'organization', 'LIST', '/organization', NULL, 'list', 40, 1, 1, '机构设置', '机构设置', '展示学校机构与部门信息', NULL, 1, 1, 0),
  (104, 0, 'MAIN_SITE', '教育教学', 'education', 'LIST', '/education', NULL, 'list', 50, 1, 1, '教育教学', '教育教学', '展示教育教学相关内容', NULL, 1, 1, 0),
  (105, 0, 'MAIN_SITE', '学生工作', 'student-work', 'LIST', '/student-work', NULL, 'list', 60, 1, 1, '学生工作', '学生工作', '展示学生工作相关内容', NULL, 1, 1, 0),
  (106, 0, 'MAIN_SITE', '公共服务', 'service', 'LIST', '/service', NULL, 'list', 70, 1, 1, '公共服务', '公共服务', '提供常用服务入口', NULL, 1, 1, 0),
  (200, 0, 'RECRUIT_SITE', '招生信息', 'admission', 'LIST', '/recruit/admission', NULL, 'list', 10, 1, 1, '招生信息', '招生,高校招生', '展示招生简章、招生动态等内容', NULL, 1, 1, 0),
  (201, 0, 'RECRUIT_SITE', '就业信息', 'employment', 'LIST', '/recruit/employment', NULL, 'list', 20, 1, 1, '就业信息', '就业,招聘', '展示就业动态、招聘信息等内容', NULL, 1, 1, 0),
  (202, 0, 'RECRUIT_SITE', '校企合作', 'school-enterprise', 'LIST', '/recruit/school-enterprise', NULL, 'list', 30, 1, 1, '校企合作', '校企合作', '展示校企合作相关内容', NULL, 1, 1, 0),
  (203, 0, 'RECRUIT_SITE', '政策公告', 'recruit-policy', 'LIST', '/recruit/policy', NULL, 'list', 40, 1, 1, '政策公告', '政策,公告', '展示招生就业政策公告', NULL, 1, 1, 0)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  column_name = VALUES(column_name),
  column_type = VALUES(column_type),
  route_path = VALUES(route_path),
  external_url = VALUES(external_url),
  template_key = VALUES(template_key),
  sort_no = VALUES(sort_no),
  nav_visible = VALUES(nav_visible),
  enabled = VALUES(enabled),
  seo_title = VALUES(seo_title),
  seo_keywords = VALUES(seo_keywords),
  seo_description = VALUES(seo_description),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO cms_site_config (
  id, site_type, config_key, config_value, config_type, description, created_by, updated_by, deleted
) VALUES
  (1, 'GLOBAL', 'siteName', '高校官网', 'STRING', '网站名称', 1, 1, 0),
  (2, 'GLOBAL', 'siteLogo', '', 'IMAGE', '网站 Logo', 1, 1, 0),
  (3, 'GLOBAL', 'icpNo', '', 'STRING', 'ICP备案号', 1, 1, 0),
  (4, 'GLOBAL', 'contactPhone', '', 'STRING', '联系电话', 1, 1, 0),
  (5, 'GLOBAL', 'contactAddress', '', 'STRING', '联系地址', 1, 1, 0),
  (6, 'GLOBAL', 'copyright', 'Copyright © 高校官网 All Rights Reserved.', 'STRING', '版权信息', 1, 1, 0),
  (7, 'MAIN_SITE', 'defaultSeoTitle', '高校官网', 'STRING', '主站默认 SEO 标题', 1, 1, 0),
  (8, 'MAIN_SITE', 'defaultSeoKeywords', '高校官网,高校,教育', 'STRING', '主站默认 SEO 关键词', 1, 1, 0),
  (9, 'MAIN_SITE', 'defaultSeoDescription', '高校官网门户网站', 'STRING', '主站默认 SEO 描述', 1, 1, 0),
  (10, 'RECRUIT_SITE', 'defaultSeoTitle', '招生就业专题站', 'STRING', '招生就业站默认 SEO 标题', 1, 1, 0),
  (11, 'RECRUIT_SITE', 'defaultSeoKeywords', '招生,就业,高校招生,高校就业', 'STRING', '招生就业站默认 SEO 关键词', 1, 1, 0),
  (12, 'RECRUIT_SITE', 'defaultSeoDescription', '高校招生就业专题站', 'STRING', '招生就业站默认 SEO 描述', 1, 1, 0),
  (13, 'MAIN_SITE', 'homeNewsLimit', '8', 'NUMBER', '首页新闻展示数量', 1, 1, 0),
  (14, 'MAIN_SITE', 'homeNoticeLimit', '6', 'NUMBER', '首页公告展示数量', 1, 1, 0)
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  config_type = VALUES(config_type),
  description = VALUES(description),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO cms_friend_link (
  id, site_type, name, link_url, logo_url, sort_no, enabled, remark, created_by, updated_by, deleted
) VALUES
  (1, 'GLOBAL', '教育部', 'https://www.moe.gov.cn/', NULL, 10, 1, '默认友情链接示例', 1, 1, 0)
ON DUPLICATE KEY UPDATE
  site_type = VALUES(site_type),
  name = VALUES(name),
  link_url = VALUES(link_url),
  logo_url = VALUES(logo_url),
  sort_no = VALUES(sort_no),
  enabled = VALUES(enabled),
  remark = VALUES(remark),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

SET FOREIGN_KEY_CHECKS = 1;
