# 高校官网后端接口系统设计文档

## 1. 项目概述

高校官网项目后端按轻量化方式建设，面向一个官网主站、一个招生就业专题站和一套 CMS 管理端提供接口。系统聚焦内容发布、栏目管理、Banner 管理、媒体库、用户角色权限、站点配置、基础 SEO、搜索和公开数据聚合能力。

项目采用前后端分离和单体模块化后端，不建设复杂站群系统。主站、招生就业专题站和 CMS 管理端的前端代码由其他团队开发；本项目只交付 `school-backend`、数据库脚本、文件存储、安全、测试、部署和接口文档。主站与专题站共用后端服务和内容模型，通过站点类型隔离栏目、内容及 Banner 数据。

本期明确不建设多语言、短信、邮件、支付、复杂 BI、深度智慧校园集成、复杂多级审核、商业全文检索和视频转码。此类需求不属于当前报价，后续提出时须重新评估架构、费用、工期和验收标准。

## 2. 建设目标

1. 为高校主站提供首页、栏目、内容、搜索、公共服务等公开数据接口。
2. 为招生就业专题站提供独立栏目、Banner、页面区块和内容数据接口。
3. 为外部 CMS 管理端提供登录认证、用户角色权限、栏目、内容、Banner、媒体库、站点配置和日志管理接口。
4. 提供标准 RESTful API，统一返回格式、错误码、鉴权和参数校验。
5. 支持 MySQL 数据持久化和本地文件存储；Redis 与 MinIO 根据实际部署需要选择启用。
6. 提供 OpenAPI、字段字典和稳定的数据契约，支持外部前端团队独立开发。
7. 控制后端复杂度，以单套部署满足轻量官网的数据服务、运维和三年后端维护需要。

## 3. 技术架构

### 3.1 总体架构

```text
外部前端应用（不在本项目交付范围）
├── 官网与专题站 portal-web
│   ├── 主站首页
│   ├── 栏目列表页
│   ├── 内容详情页
│   ├── 搜索页
│   └── 招生就业专题站
│
├── 后台管理端 admin-web
│   ├── 登录认证
│   ├── 系统管理
│   ├── CMS 管理
│   └── 日志管理
│
└── Nginx / API Gateway
    ├── API 反向代理
    └── 上传文件访问代理

后端服务 school-backend
├── Admin API
├── Portal API
├── Auth & Security
├── CMS Domain
├── File Storage
└── Infrastructure

基础设施
├── MySQL 8
├── Local Storage
├── Redis / MinIO（按需启用）
└── OpenAPI / Swagger UI
```

### 3.2 推荐技术栈

| 层级 | 技术 |
| --- | --- |
| 后端语言 | Java 25 |
| 构建工具 | Gradle 9.x，编译与运行目标均为 Java 25 |
| 后端框架 | Spring Boot 4.1.0 |
| 安全框架 | Spring Security OAuth2 Resource Server + Spring JwtEncoder/JwtDecoder |
| 数据访问 | MyBatis Spring Boot Starter 4.0.1 / MyBatis |
| 数据库 | MySQL 8.x |
| 缓存 | Redis，可选启用 |
| 文件存储 | 本地文件存储，保留 MinIO 适配接口 |
| API 文档 | Springdoc OpenAPI 3.0.3 / Swagger UI |
| 接口调用方 | 外部 CMS、官网和专题站前端，不限定技术栈 |
| 部署入口 | Nginx + Java 25 服务 |

### 3.3 Spring Boot 4 依赖基线

Spring Boot 4 对 starter 和自动配置模块进行了拆分或重命名。工程升级时统一采用以下目标依赖，避免只修改 Boot 版本号造成构建或运行失败：

| 能力 | Boot 4 目标依赖或版本 |
| --- | --- |
| Spring MVC | `spring-boot-starter-webmvc` |
| AOP | `spring-boot-starter-aspectj` |
| OAuth2 Resource Server | `spring-boot-starter-security-oauth2-resource-server` |
| MyBatis | `mybatis-spring-boot-starter:4.0.1` |
| OpenAPI | `springdoc-openapi-starter-webmvc-ui:3.0.3` |
| 测试 | 按模块使用 Boot 4 test starter，并使用 `spring-boot-starter-security-test` |

Boot 4 默认使用 Jackson 3。现有 Jackson 2 代码应整体迁移到 Jackson 3 包名；确需过渡时可短期使用 Boot 提供的 Jackson 2 兼容模块，但不得长期混用。自动配置排除项和测试注解也必须按 Boot 4 的新包名复核。

版本基线核对日期为 2026-07-16，依据 [Spring Boot 4.1 发布说明](https://spring.io/blog/2026/06/10/spring-boot-4/)、[Spring Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html)、[Spring Boot 4 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)、[MyBatis Spring Boot 兼容矩阵](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/) 和 [springdoc v4 文档](https://springdoc.org/v4/index.html)。

## 4. 应用边界

### 4.1 后端服务

后端提供后台管理 API、官网公开 API、文件上传访问、认证鉴权、日志记录和基础配置能力。后端不负责页面渲染，页面由前端工程通过 API 获取数据。

建议包结构：

```text
src/main/java/com/zlwang/school/
├── SchoolBackendApplication.java
├── common/
│   ├── api/
│   ├── exception/
│   ├── pagination/
│   └── validation/
├── config/
├── security/
├── modules/
│   ├── auth/
│   ├── user/
│   ├── role/
│   ├── permission/
│   ├── column/
│   ├── content/
│   ├── banner/
│   ├── media/
│   ├── site/
│   ├── link/
│   ├── seo/
│   └── log/
└── infrastructure/
    ├── cache/
    ├── storage/
    └── persistence/
```

### 4.2 Admin API 边界

Admin API 面向外部 CMS 管理端，提供认证、账号权限、栏目、内容、附件、模板注册表、页面区块、Banner、媒体、友情链接、站点配置、SEO 和日志接口。首版采用简化发布流程，不建设跨部门多级审核工作流。

后端返回模板注册表和 `editorSchema`，并负责保存时的字段白名单、类型、范围和站点归属校验。前端根据契约渲染编辑控件，但前端隐藏字段或绕过控件不能绕过服务端校验。本项目不实现 CMS 页面、组件和前端路由。

### 4.3 Portal API 边界

Portal API 面向外部官网和招生就业专题站前端，提供站点配置、导航、Banner、页面区块、栏目树、内容分页、内容详情、友情链接、搜索、附件和浏览量接口。

公开接口原则上只暴露已启用、已发布、未删除且当前有效的数据。后端提供页面模板编码、结构化内容和 SEO 回退数据，不负责 HTML 渲染、页面路由、响应式布局、浏览器兼容、SSR 或静态化。

## 5. 核心业务模块

### 5.1 认证与权限

认证模块负责后台登录、JWT 签发、当前用户信息查询、退出登录和密码修改。后台接口默认需要登录，公开 API 不需要登录。账号密码仍由本系统校验，登录成功后使用 Spring `JwtEncoder` 签发访问令牌；后续请求由 Spring Security OAuth2 Resource Server 调用 `JwtDecoder` 完成签名、签发方和有效期校验。本项目不建设 OAuth2 授权服务器。

JWT 契约如下：

| Claim | 用途 |
| --- | --- |
| `iss` | 签发方，必须与服务端配置一致 |
| `sub` | 用户名，作为账号查询标识 |
| `uid` | 用户 ID，用于审计和一致性检查 |
| `authorities` | `ROLE_` 前缀角色编码与权限编码列表 |
| `iat` | 签发时间 |
| `exp` | 过期时间 |

令牌使用 HS256。`JWT_SECRET` 必须是 Base64 字符串，解码后不少于 32 字节，签发端和校验端必须复用同一 `SecretKey`。生产环境必须显式配置密钥，本地环境只能使用独立开发密钥。

Resource Server 完成 JWT 基础校验后，认证转换器必须按 `sub` 重新查询账号、角色和权限，拒绝不存在、已删除或已禁用账号，并以 `AuthenticatedUser` 作为 principal。授权信息以数据库当前状态为准，使账号禁用和权限调整立即生效；JWT 中的 `authorities` 仅作为已签名快照，不作为绕过数据库状态检查的依据。这样可保持 `/api/admin/auth/me`、用户写操作的操作者 ID 和现有方法级权限校验契约一致。

权限模型采用用户、角色、权限三层结构：

```text
sys_user
sys_role
sys_permission
sys_user_role
sys_role_permission
```

首版角色建议：

| 角色 | 权限范围 |
| --- | --- |
| 超级管理员 | 全部功能 |
| 网站管理员 | 栏目、内容、Banner、媒体库、站点配置 |
| 内容编辑 | 指定栏目内容维护 |
| 内容审核员 | 预留审核权限，首版可不启用审核流 |
| 招生就业管理员 | 招生就业专题站栏目和内容维护 |

### 5.2 栏目管理

栏目是官网导航、内容聚合和专题站结构的基础。栏目支持树形结构、排序、启用禁用、导航显示控制、SEO 配置、站点类型和页面模板选择。模板编码必须来自固定注册表，栏目只能引用与站点和栏目类型兼容的模板。

栏目类型：

```text
PAGE      单页栏目
LIST      列表栏目
IMAGE     图片栏目
DOWNLOAD  下载栏目
LINK      外链栏目
SPECIAL   专题栏目
```

站点类型：

```text
MAIN_SITE     主站
RECRUIT_SITE  招生就业专题站
```

### 5.3 内容管理

内容管理支持文章维护、草稿、发布、下线、置顶、推荐、封面图、附件、浏览量和 SEO 信息。内容归属于栏目，通过栏目站点类型区分主站和招生就业专题站内容。公共字段固定存储，机构、服务和单页等模板的少量专属字段存入经过模板白名单校验的 `extension_data`，不为普通文章类型重复建表。

内容状态：

```text
DRAFT      草稿
PUBLISHED  已发布
OFFLINE    已下线
DELETED    已删除
```

内容保存与发布遵循以下规则：

1. `POST /api/admin/contents` 只创建 `DRAFT` 草稿，普通编辑接口不直接改变发布状态。
2. 内容生效模板优先使用栏目 `detail_template_key`，未配置详情模板时使用 `template_key`；无内容字段的首页、专题页、系统页和外链栏目禁止维护普通内容。
3. 草稿允许暂缺正文、发布时间或模板扩展必填字段；发布时必须一次性校验栏目启用状态、公共必填字段和扩展必填字段。
4. `PUT /api/admin/contents/{id}/publish` 支持传入 `publishAt`，未传时使用服务器当前时间；定时内容保持 `PUBLISHED` 状态，由公开 API 按 `publish_at` 过滤未到发布时间的数据。
5. `extension_data` 仅接受固定模板注册表声明的字段，服务端校验字段名、类型、长度、数值范围和选项，并补齐模板默认值；其中禁止保存 HTML、脚本和组件代码。
6. `content_html` 使用基于 HTML 解析器的安全白名单清洗，移除脚本、事件属性和危险协议，同时保留受控富文本标签及 `/uploads/...` 相对资源地址。
7. 内容新增和编辑时，附件列表与内容在同一事务中保存；编辑采用整组替换语义，单篇内容最多维护 50 个附件。
8. 内容所属 `site_type` 始终从栏目派生，客户端不能单独指定站点，避免主站与招生就业专题站内容串站。

### 5.4 Banner 管理

Banner 支持主站首页、招生就业专题站和栏目页公共展示位置。Banner 可配置标题、副标题、桌面图片、移动图片、跳转方式、排序、启用状态和展示时间范围。移动图片未配置时由前端使用桌面图片的安全裁切方案。

展示位置：

```text
HOME
RECRUIT_HOME
COLUMN
```

位置与站点遵循固定匹配规则：`HOME` 只属于 `MAIN_SITE`，`RECRUIT_HOME` 只属于 `RECRUIT_SITE`，`COLUMN` 可用于两个站点的栏目页公共兜底主视觉。单个栏目的专属头图继续使用 `cms_column.cover_url`，`cms_banner.link_ref_id` 只表示跳转目标，不复用为 Banner 展示栏目标识。

跳转类型：

```text
NONE      不跳转，link_ref_id 和 link_url 均为空
CONTENT   跳转内容，只设置 link_ref_id
COLUMN    跳转栏目，只设置 link_ref_id
EXTERNAL  跳转外部地址，只设置 link_url
```

外部地址仅允许有效的 HTTP 或 HTTPS URL。内部内容和栏目必须与 Banner 属于同一站点；启用 Banner 时，目标内容必须处于 `PUBLISHED` 状态，目标栏目必须启用。禁用 Banner 可提前引用草稿内容或停用栏目用于运营准备，但仍要求目标存在且同站点。

`start_time` 与 `end_time` 均可为空；同时填写时 `start_time` 必须早于 `end_time`，管理接口使用 ISO-8601 本地日期时间格式。`enabled` 控制运营开关，公开 API 还需同时判断当前时间处于生效区间。启用 Banner 引用的内容不得下线，启用 Banner 引用的栏目不得停用；被任意未删除 Banner 引用的内容或栏目不得删除，需先解除或删除 Banner 引用。

### 5.5 媒体库

媒体库负责图片、文档、视频和压缩包的上传、分页查询、详情、公开访问和删除。首版启用本地文件存储，通过 `StorageService` 隔离业务与存储实现；MinIO 仅保留存储类型和适配接口边界，本期不引入 MinIO 客户端或部署依赖。

文件类型：

```text
IMAGE     jpg、jpeg、png、gif、webp
DOCUMENT  pdf、doc、docx、xls、xlsx、ppt、pptx、txt、csv
VIDEO     mp4、webm、mov
OTHER     zip
```

上传接口同时校验非空文件、文件大小、扩展名白名单和扩展名对应的 MIME 大类。原始文件名只用于展示，服务端使用 UUID 生成存储文件名，并按 `yyyy/MM` 目录保存；数据库中的 `file_path` 始终保存相对路径，不能由客户端指定。

本地文件通过配置的 `public-url-prefix` 提供匿名 `GET` 访问，默认地址形如 `/uploads/2026/07/{uuid}.jpg`。生产环境可由 Nginx 直接映射上传目录，应用内资源映射主要用于 local 环境和单体部署。

内容附件允许填写 `mediaId` 引用媒体库。引用存在时，后端以媒体库中的访问地址、文件大小和文件类型覆盖客户端提交值，防止伪造附件元数据；仍被未删除内容附件引用的媒体文件不能删除。删除操作同步逻辑删除媒体元数据和清理本地物理文件。

### 5.6 站点配置与友情链接

站点配置用于维护网站名称、Logo、备案号、联系电话、联系地址、版权信息、首页展示数量和默认 SEO 信息。配置作用域独立定义为 `GLOBAL`、`MAIN_SITE` 和 `RECRUIT_SITE`；`GLOBAL` 表示两个站点共用，不能加入只包含主站和专题站的页面模板 `SiteType`。

本期配置键由初始化 SQL 固定，Admin API 只允许按作用域批量修改配置值，不允许动态新增、删除或修改配置键、类型和说明。批量更新在同一事务中完成，未知键、重复键或任意类型校验失败时整批不保存。

| 作用域 | 固定配置 |
| --- | --- |
| `GLOBAL` | `siteName`、`siteLogo`、`icpNo`、`contactPhone`、`contactAddress`、`copyright` |
| `MAIN_SITE` | `defaultSeoTitle`、`defaultSeoKeywords`、`defaultSeoDescription`、`homeNewsLimit`、`homeNoticeLimit` |
| `RECRUIT_SITE` | `defaultSeoTitle`、`defaultSeoKeywords`、`defaultSeoDescription` |

配置类型支持 `STRING`、`NUMBER`、`BOOLEAN`、`JSON` 和 `IMAGE`。首页展示数量必须是 1-100 的整数；图片值允许空值、无路径跳转的站内绝对路径或有效 HTTP/HTTPS 地址。JSON、布尔值和数字即使前端已校验，保存时仍由后端按 `config_type` 再次验证。

友情链接支持 `GLOBAL`、`MAIN_SITE` 和 `RECRUIT_SITE` 三类作用域，以及名称、链接地址、Logo、排序、启用禁用和备注。跳转地址只允许有效 HTTP/HTTPS URL；Logo 可使用安全的站内绝对路径或有效 HTTP/HTTPS URL。列表按 `sortNo`、`id` 升序返回，删除采用逻辑删除并同时停用。

### 5.7 基础 SEO

栏目、内容和站点默认配置共同形成 SEO 数据。后台预览与后续 Portal API 共用 `SeoMetadataService`，统一返回 `title`、`keywords`、`description` 和 `canonicalPath`，避免各接口分别实现回退规则。

栏目回退规则：

```text
title        栏目 SEO 标题 > 栏目名称 > 站点默认标题
keywords     栏目 SEO 关键词 > 站点默认关键词
description  栏目 SEO 描述 > 站点默认描述
```

内容回退规则：

```text
title        内容 SEO 标题 > 栏目 SEO 标题 > 内容标题 > 栏目名称 > 站点默认标题
keywords     内容 SEO 关键词 > 栏目 SEO 关键词 > 站点默认关键词
description  内容 SEO 描述 > 栏目 SEO 描述 > 内容摘要 > 站点默认描述
```

栏目 canonical 路径优先使用 `route_path`，为空时回退为 `/columns/{id}`；内容路径使用 `{栏目 canonical 路径}/{contentId}`。后端只返回 canonical 路径，域名拼接和 HTML 标签渲染由前端负责。

### 5.8 搜索

首版搜索基于数据库查询实现，支持关键词匹配标题、按站点隔离、按栏目筛选、发布时间倒序和分页展示。搜索不匹配正文、摘要或附件，不提供关键词高亮、热门搜索或搜索日志统计。公开查询同时过滤停用栏目、草稿、下线和未到发布时间的内容。后续如需全文检索，可接入 Elasticsearch 或 MySQL Fulltext。

### 5.9 日志

登录服务在账号密码校验完成后记录成功或失败结果，包括账号、用户 ID、客户端 IP、User-Agent 和通用失败原因。认证失败不记录明文密码，也不通过失败原因暴露账号是否存在。

后台写操作通过安全过滤链统一审计，覆盖 `/api/admin/**` 下的 `POST`、`PUT`、`PATCH` 和 `DELETE`，登录接口单独使用登录日志。操作日志保存用户、模块、动作、请求方法、路径、IP、参数摘要、结果、失败摘要和耗时；日志查询等只读请求不重复记录。

参数摘要按结构化 JSON 处理，字段名包含 `password`、`secret`、`token`、`authorization` 或 `credential` 时统一替换为 `***`。非 JSON 请求不保存原始请求体，摘要最长 2000 个字符。审计持久化异常只写应用告警日志，不覆盖原业务响应。

## 6. 数据模型设计

### 6.1 基础字段约定

业务表统一包含以下审计字段：

```text
id          bigint primary key
created_at  datetime
updated_at  datetime
created_by  bigint
updated_by  bigint
deleted     tinyint
```

建议使用逻辑删除，查询默认过滤 `deleted = 0`。

### 6.2 表清单

| 表名 | 用途 |
| --- | --- |
| sys_user | 后台用户 |
| sys_role | 角色 |
| sys_permission | 菜单和按钮权限 |
| sys_user_role | 用户角色关系 |
| sys_role_permission | 角色权限关系 |
| cms_column | 栏目 |
| cms_content | 内容 |
| cms_content_attachment | 内容附件 |
| cms_banner | Banner |
| cms_page_section | 主站首页和专题首页的预定义页面区块 |
| cms_media | 媒体文件 |
| cms_site_config | 站点配置 |
| cms_friend_link | 友情链接 |
| sys_operation_log | 操作日志 |
| sys_login_log | 登录日志 |

### 6.3 关键关系

```text
sys_user n -- n sys_role
sys_role n -- n sys_permission
cms_column 1 -- n cms_column
cms_column 1 -- n cms_content
cms_content 1 -- n cms_content_attachment
cms_media 1 -- n cms_content_attachment
cms_column 1 -- n cms_page_section (data source)
```

招生就业专题站首版优先复用 `cms_column` 和 `cms_content`，通过 `site_type = RECRUIT_SITE` 区分，避免重复建模。`cms_column` 增加详情模板和模板配置，`cms_content` 增加受控扩展数据，`cms_page_section` 作为模板方案落地时的数据库增量，具体字段见 14.6 节。

## 7. API 设计

### 7.1 API 分组

```text
/api/admin/**   后台管理 API，需要认证和权限校验
/api/portal/**  官网公开 API，不需要后台登录
/api/files/**   文件访问 API，根据文件公开策略控制访问
```

### 7.2 统一返回格式

普通接口：

```json
{
  "code": "000000",
  "msg": "success",
  "data": {}
}
```

分页接口：

```json
{
  "code": "000000",
  "msg": "success",
  "data": {
    "records": [],
    "total": 100,
    "pageNo": 1,
    "pageSize": 10
  }
}
```

错误接口：

```json
{
  "code": "A0400",
  "msg": "参数校验失败",
  "data": null
}
```

### 7.3 核心接口

后台认证：

```text
POST /api/admin/auth/login
POST /api/admin/auth/logout
GET  /api/admin/auth/me
PUT  /api/admin/auth/password
```

角色管理：

```text
GET    /api/admin/roles
GET    /api/admin/roles/{id}
POST   /api/admin/roles
PUT    /api/admin/roles/{id}
PUT    /api/admin/roles/{id}/permissions
DELETE /api/admin/roles/{id}
```

权限管理：

```text
GET    /api/admin/permissions/tree
GET    /api/admin/permissions/{id}
POST   /api/admin/permissions
PUT    /api/admin/permissions/{id}
DELETE /api/admin/permissions/{id}
```

栏目管理：

```text
GET    /api/admin/page-templates
GET    /api/admin/columns/tree
GET    /api/admin/columns/{id}/editor-schema
POST   /api/admin/columns
PUT    /api/admin/columns/{id}
DELETE /api/admin/columns/{id}
PUT    /api/admin/columns/{id}/status
PUT    /api/admin/columns/sort
```

内容管理：

```text
GET    /api/admin/contents
GET    /api/admin/contents/{id}
POST   /api/admin/contents
PUT    /api/admin/contents/{id}
DELETE /api/admin/contents/{id}
PUT    /api/admin/contents/{id}/publish
PUT    /api/admin/contents/{id}/offline
PUT    /api/admin/contents/{id}/top
PUT    /api/admin/contents/{id}/recommend
```

内容列表支持 `keyword`、`columnId`、`siteType`、`status`、`pageNo` 和 `pageSize` 查询参数。读取接口要求 `cms:content` 权限，新增、编辑、删除及状态动作要求 `cms:content:manage` 权限。`top` 请求体使用 `topFlag`，`recommend` 请求体使用 `recommendFlag`，发布请求体中的 `publishAt` 可省略。

页面区块管理：

```text
GET /api/admin/pages/{pageCode}/sections
PUT /api/admin/pages/{pageCode}/sections
```

Banner 管理：

```text
GET    /api/admin/banners
GET    /api/admin/banners/{id}
POST   /api/admin/banners
PUT    /api/admin/banners/{id}
PUT    /api/admin/banners/{id}/status
PUT    /api/admin/banners/sort
DELETE /api/admin/banners/{id}
```

Banner 列表支持 `keyword`、`siteType`、`position`、`enabled`、`pageNo` 和 `pageSize` 查询参数，并按 `sortNo`、`id` 升序返回。读取接口要求 `cms:banner` 权限，新增、编辑、排序、启停和删除要求 `cms:banner:manage` 权限。

媒体库管理：

```text
GET    /api/admin/media
GET    /api/admin/media/{id}
POST   /api/admin/media/upload
DELETE /api/admin/media/{id}
```

媒体列表支持 `keyword`、`fileType`、`storageType`、`uploaderId`、`pageNo` 和 `pageSize` 查询参数，并按创建时间、ID 倒序返回。上传使用 `multipart/form-data`，文件字段名为 `file`，可选备注字段名为 `remark`。读取接口要求 `cms:media` 权限，上传和删除要求 `cms:media:manage` 权限；公开文件地址只允许匿名 `GET`，媒体元数据管理接口仍需鉴权。

站点配置管理：

```text
GET /api/admin/site-config
PUT /api/admin/site-config/{siteType}
```

配置列表可使用 `siteType` 筛选；不传时按 `GLOBAL`、`MAIN_SITE`、`RECRUIT_SITE` 返回全部固定配置。更新请求包含 `items` 数组，每项只提交 `configKey` 和 `configValue`。读取要求 `cms:site-config` 权限，更新要求 `cms:site-config:manage` 权限。

友情链接管理：

```text
GET    /api/admin/friend-links
GET    /api/admin/friend-links/{id}
POST   /api/admin/friend-links
PUT    /api/admin/friend-links/{id}
PUT    /api/admin/friend-links/{id}/status
PUT    /api/admin/friend-links/sort
DELETE /api/admin/friend-links/{id}
```

友情链接列表支持 `keyword`、`siteType`、`enabled`、`pageNo` 和 `pageSize` 查询参数。读取要求 `cms:friend-link` 权限，新增、编辑、排序、启停和删除要求 `cms:friend-link:manage` 权限。

SEO 预览：

```text
GET /api/admin/seo/columns/{id}
GET /api/admin/seo/contents/{id}
```

栏目 SEO 预览复用 `cms:column` 权限，内容 SEO 预览复用 `cms:content` 权限，不增加独立 SEO 权限点。

日志查询：

```text
GET /api/admin/logs/operations
GET /api/admin/logs/login
```

操作日志支持 `username`、`moduleName`、`operationType`、`resultStatus`、`startTime`、`endTime`、`pageNo` 和 `pageSize` 筛选，要求 `log:operation` 权限。登录日志支持 `username`、`loginStatus`、`startTime`、`endTime`、`pageNo` 和 `pageSize` 筛选，要求 `log:login` 权限。时间参数使用 ISO-8601 本地日期时间，开始时间不能晚于结束时间。

前台公开 API：

```text
GET /api/portal/site-config?siteType={siteType}
GET /api/portal/navigation?siteType={siteType}
GET /api/portal/banners?siteType={siteType}&position={position}
GET /api/portal/columns?siteType={siteType}
GET /api/portal/columns/{id}
GET /api/portal/columns/{id}/contents?pageNo={pageNo}&pageSize={pageSize}
GET /api/portal/contents/{id}
PUT /api/portal/contents/{id}/view-count
GET /api/portal/search?keyword={keyword}&siteType={siteType}&columnId={columnId}&pageNo={pageNo}&pageSize={pageSize}
GET /api/portal/friend-links?siteType={siteType}
GET /api/portal/recruit/home
GET /api/portal/pages/{pageCode}
```

站点公共查询中的 `siteType` 必须是 `MAIN_SITE` 或 `RECRUIT_SITE`，不接受仅用于配置和友情链接作用域的 `GLOBAL`。Banner 查询还必须传入 `HOME`、`RECRUIT_HOME` 或 `COLUMN` 位置，首页位置与站点不匹配时返回参数错误。

## 8. 安全设计

1. 后台接口使用 Spring Security OAuth2 Resource Server + JWT 鉴权，JWT 签发与校验统一使用 Spring `JwtEncoder`、`JwtDecoder`，不直接依赖 JJWT。
2. 密码使用 BCrypt 加密存储。若使用 `DelegatingPasswordEncoder`，密码字段统一保存 `{bcrypt}` 前缀；升级前已有的无前缀 BCrypt 数据须迁移或配置明确的兼容编码器。
3. 未登录访问后台接口返回 401。
4. 无权限访问后台接口返回 403。
5. 文件上传限制大小、后缀和 MIME 类型。
6. 富文本内容进入前台展示前进行安全处理，避免 XSS。
7. 后台写操作记录操作日志，包含用户、IP、接口、脱敏参数摘要、执行结果和耗时；密码、令牌和密钥不得进入日志。
8. 管理员账号支持启用禁用和密码重置。
9. 模板编码、区块类型和 JSON 配置使用服务端白名单及结构校验，禁止通过配置注入脚本、组件路径或任意查询条件。
10. 外部链接仅允许 `http`、`https` 等约定协议，后台保存时校验协议，前台输出时进行安全属性处理。

### 8.1 Security 迁移验收条件

1. Gradle 依赖树中不存在任何 `io.jsonwebtoken` 依赖，旧 JWT service 和 filter 已移除或不再参与编译。
2. 登录签发的令牌可被同一服务的 `JwtDecoder` 校验，并包含完整 JWT 契约。
3. `@AuthenticationPrincipal` 可稳定取得 `AuthenticatedUser`，当前用户接口和所有需要操作者 ID 的写接口通过测试。
4. 用户禁用、删除或权限变更后，旧令牌在下一次请求立即按数据库当前状态处理。
5. 初始化管理员密码和后续新增、重置密码采用同一种可验证的存储格式。
6. 401、403 保持统一 JSON 响应；过期、伪造、签发方不匹配和权限不足场景均有自动化测试。
7. `./gradlew clean test` 在 Java 25 下通过，应用可使用 local 配置启动。

## 9. 缓存设计

首版不启用 Spring Cache、Redis 或本地结果缓存。Portal 页面聚合、站点配置、导航、Banner、友情链接、栏目、内容和搜索均在每次请求时读取当前仓储数据，后台发布、下线、启停或配置修改后，下一次 Portal 请求必须立即反映新状态。Portal 响应保留 Spring Security 默认的 `Cache-Control: no-cache, no-store` 等缓存控制头，避免浏览器或中间代理返回陈旧结果。这样符合当前轻量化访问规模，也避免在没有明确容量数据时引入缓存一致性和运维成本。

若后续监控表明确显示数据库读取成为瓶颈，可按以下 Key 规划启用 Redis：

| 缓存对象 | 预留 Key 示例 | 启用后的失效策略 |
| --- | --- | --- |
| 站点配置 | `site:config:{siteType}` | 修改全局或对应站点配置后删除 |
| 导航栏目 | `site:navigation:{siteType}` | 修改栏目后删除 |
| 首页 Banner | `site:banner:{siteType}:{position}` | 修改 Banner 后删除 |
| 友情链接 | `site:friend-links:{siteType}` | 修改全局或对应站点友情链接后删除 |

未来启用缓存时，必须同时实现后台写操作主动失效、TTL 兜底、站点隔离和并发一致性测试，不能只添加读缓存。内容详情、列表、搜索和浏览量默认继续直查；浏览量写入不使用普通结果缓存。

## 10. 文件存储设计

定义统一存储接口：

```text
StorageService
├── LocalStorageService
└── MinioStorageService
```

配置示例：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 21MB
app:
  file:
    storage-type: local
    local-path: /data/school/uploads
    public-url-prefix: /uploads
    max-size: 20MB
    allowed-extensions:
      - jpg
      - jpeg
      - png
      - gif
      - webp
      - pdf
      - doc
      - docx
      - xls
      - xlsx
      - ppt
      - pptx
      - txt
      - csv
      - mp4
      - webm
      - mov
      - zip
```

`spring.servlet.multipart` 负责在 Web 容器层限制请求大小，`app.file.max-size` 负责业务层校验，生产配置应保持两者一致并为 multipart 请求保留少量协议开销。`public-url-prefix` 必须是非根相对 URL 前缀且不能包含路径跳转；本地存储实现还会校验最终路径必须位于上传根目录内。

## 11. 部署设计

### 11.1 环境划分

```text
local  本地开发环境
test   测试环境
prod   生产环境
```

### 11.2 部署拓扑

```text
Nginx
├── /api           school-backend 反向代理
└── /uploads       文件访问

school-backend (Java 25)
├── MySQL
├── Local Storage
└── Redis / MinIO（按需启用）
```

## 12. 质量保障

1. 单元测试覆盖统一返回、异常处理、核心 Service 和权限判断。
2. 接口测试覆盖认证、栏目、内容、Banner、媒体库和 Portal API。
3. 权限测试覆盖未登录、无权限、不同角色访问边界。
4. 文件上传测试覆盖大小限制、后缀限制、访问地址和删除逻辑。
5. Portal API 测试覆盖首页聚合、栏目、详情、搜索、空数据、发布时间和有效时间过滤。
6. 模板数据契约测试覆盖八类模板的字段定义、必填校验、数据来源和 SEO 回退。
7. 页面区块测试覆盖排序、启停、栏目引用、跨站点拦截、配置校验和缓存失效。
8. 上线前执行数据库初始化脚本、模板增量 SQL、部署脚本和回滚方案验证。

浏览器兼容、响应式、视觉回归、前端性能和页面可访问性测试由外部前端团队负责，不计入本后端质量门槛。

## 13. 范围约束与变更原则

当前架构只覆盖 `school-backend`、Admin API、Portal API、MySQL、文件存储、基础 SEO 数据、接口联调、后端部署和三年后端维护。原前后端整包报价已经失效，后端范围须单独核价。

以下能力不在本期设计范围内：

```text
官网、专题站和 CMS 管理端前端代码
UI/UX、响应式、浏览器兼容和前端部署
复杂站群和多租户建站
多语言
短信、邮件和支付
复杂 BI 和数据大屏
统一身份认证及智慧校园业务系统深度集成
在线报名、在线缴费和复杂互动业务
内容多级审核流
商业全文检索和视频转码
旧站全量数据迁移
等保测评和专项整改
```

后续新增范围时，应先形成书面变更单，说明业务目标、数据来源、接口责任、架构影响、费用、工期和验收标准，经确认后进入实施计划。

## 14. 页面模板数据契约设计

后端数据体系采用“**栏目选择模板，模板决定字段契约，外部前端按模板渲染**”的方式。模板是受控的后端业务类型，不是可由管理员上传代码或任意拖拽生成页面的低代码能力，也不代表本项目交付前端模板代码。

也就是说，后台新增栏目时先选择页面类型：

```text
学校新闻 → 新闻列表模板
学校简介 → 单页图文模板
机构设置 → 机构展示模板
公共服务 → 服务入口模板
招生就业 → 招生就业专题模板
```

Admin API 返回对应模板的 `editorSchema`，由外部 CMS 前端决定如何展示编辑控件；无论前端如何渲染，保存请求都必须通过后端模板校验。

### 14.1 本期模板基线

后端专项固定交付八类模板的数据契约、编辑 schema、保存校验和 Portal API 输出规则。

| 模板编码 | 模板名称 | 适用场景 | 主要数据来源 | 后端交付 |
| --- | --- | --- | --- | --- |
| `HOME` | 官网首页 | 主站首页 | 站点配置、Banner、页面区块 | 数据契约与聚合接口 |
| `ARTICLE_LIST` | 文章列表页 | 新闻、通知、招生信息 | 栏目、内容分页 | 数据契约与分页接口 |
| `ARTICLE_DETAIL` | 文章详情页 | 新闻和普通文章详情 | 内容、附件 | 数据契约与详情接口 |
| `SINGLE_PAGE` | 单页图文页 | 学校简介、办学理念、联系我们 | 单条内容、附件 | 数据契约与详情接口 |
| `ORGANIZATION` | 机构展示页 | 院系、行政部门、教学单位 | 栏目及结构化内容 | 数据契约与聚合接口 |
| `SERVICE_DIRECTORY` | 公共服务页 | OA、图书馆、校园服务入口 | 分类及结构化内容 | 数据契约与聚合接口 |
| `RECRUIT_HOME` | 招生就业专题首页 | 招生就业专题入口 | 专题配置、Banner、页面区块 | 数据契约与聚合接口 |
| `SEARCH_RESULT` | 搜索结果页 | 全站文章搜索 | 搜索 API | 搜索数据契约 |

在线报名和咨询留言可继续保留为二期数据契约参考，但涉及公众数据采集、验证码、隐私授权、脱敏、导出和审核，本期不建表、不开发接口、不进入后端验收范围。专题中的“报名入口”“留言入口”本期仅支持配置外部链接。

本期模板边界如下：

1. 模板编码由代码和版本管理固定维护，管理员不能新增可执行模板。
2. 首页和专题首页允许配置预定义区块的标题、数据来源、数量、样式、排序和启停，不支持自由拖拽、任意嵌套或上传组件。
3. 普通栏目复用文章列表、详情和单页数据契约；新增同结构栏目不新增后端模板类型。
4. 新增不同数据结构、特殊交互所需接口或新的聚合模型时，须按变更流程评估工期和费用。

### 14.2 整体设计思路

后台数据建议分成三层：

#### 14.2.1 栏目配置

决定这个栏目使用什么模板、前台如何展示。

例如：

| 字段       | 示例              |
| -------- | --------------- |
| 栏目名称     | 学校新闻            |
| 栏目编码     | school-news     |
| 父级栏目     | 新闻中心            |
| 页面模板     | 新闻列表模板          |
| 详情模板     | 新闻详情模板          |
| 导航显示     | 是               |
| 排序       | 10              |
| SEO标题    | 学校新闻            |
| 页面Banner | news-banner.jpg |

#### 14.2.2 内容数据

填写新闻、文章、招生信息等具体内容。

例如：

| 字段   | 示例             |
| ---- | -------------- |
| 标题   | 学校举行2026年开学典礼  |
| 摘要   | 学校新学期开学典礼顺利举行  |
| 封面图  | news-cover.jpg |
| 正文   | 富文本内容          |
| 发布时间 | 2026-09-01     |
| 来源   | 党委宣传部          |
| 附件   | 活动安排.pdf       |

#### 14.2.3 页面区块配置

主要用于首页、专题首页等复杂页面。

例如首页可以由这些区块组成：

```text
轮播图
学校新闻
通知公告
招生就业入口
校园风采
公共服务
友情链接
```

每个区块可以单独配置标题、展示栏目、展示数量和排列方式。

---

### 14.3 公共字段设计

不管是哪种内容模板，建议统一保留一组公共字段。

#### 14.3.1 公共内容字段

| 字段名称   | 字段编码           | 类型   | 是否必填 | 说明            |
| ------ | -------------- | ---- | ---- | ------------- |
| 内容标题   | title          | 单行文本 | 是    | 前台显示的主标题      |
| 副标题    | subtitle       | 单行文本 | 否    | 标题补充          |
| 所属栏目   | columnId       | 栏目选择 | 是    | 内容发布到哪个栏目     |
| 内容摘要   | summary        | 多行文本 | 否    | 列表页简要介绍       |
| 封面图片   | coverUrl       | 图片   | 否    | 列表页或推荐区域展示    |
| 作者     | author         | 单行文本 | 否    | 文章作者          |
| 来源     | source         | 单行文本 | 否    | 内容来源          |
| 正文内容   | contentHtml    | 富文本  | 按模板  | 文章主要内容        |
| 发布时间   | publishAt      | 日期时间 | 是    | 支持立即或定时发布     |
| 附件     | attachments    | 多文件  | 否    | PDF、Word等下载文件 |
| 是否置顶   | topFlag        | 开关   | 否    | 在栏目列表顶部展示     |
| 是否推荐   | recommendFlag  | 开关   | 否    | 是否推荐到首页       |
| 排序号    | sortNo         | 数字   | 否    | 数字越小越靠前       |
| 发布状态   | status         | 枚举   | 是    | 草稿、已发布、已下线     |
| SEO标题  | seoTitle       | 单行文本 | 否    | 搜索引擎页面标题      |
| SEO关键词 | seoKeywords    | 单行文本 | 否    | SEO关键词        |
| SEO描述  | seoDescription | 多行文本 | 否    | 搜索结果摘要        |

公共字段尽量固定，不建议每个模板都重复定义。

---

### 14.4 模板字段设计

#### 14.4.1 官网首页模板 `HOME`

首页不适合按普通文章维护，建议使用“首页区块配置”。

##### 基础信息

| 字段      | 类型 | 说明        |
| ------- | -- | --------- |
| 首页标题    | 文本 | 浏览器标题     |
| 首页Logo  | 图片 | 学校Logo    |
| 首页背景图   | 图片 | 可选        |
| 首页SEO信息 | 文本 | 标题、关键词、描述 |
| 首页状态    | 开关 | 是否启用      |

学校名称、Logo、版权和联系方式优先复用 `cms_site_config`，首页模板不重复维护站点级数据。首页专属主视觉和展示内容由 Banner、页面区块及模板配置提供。

##### 轮播图区域

每张轮播图字段：

| 字段     | 类型                 |
| ------ | ------------------ |
| 轮播标题   | 文本                 |
| 轮播副标题  | 文本                 |
| 图片     | 图片上传               |
| 手机端图片  | 图片上传               |
| 跳转类型   | 无跳转、内部文章、内部栏目、外部链接 |
| 跳转地址   | 文本或内容选择            |
| 展示开始时间 | 日期时间               |
| 展示结束时间 | 日期时间               |
| 排序     | 数字                 |
| 是否启用   | 开关                 |

##### 新闻区块

| 字段     | 说明           |
| ------ | ------------ |
| 区块标题   | 例如“学校新闻”     |
| 数据来源栏目 | 选择新闻中心       |
| 展示数量   | 例如6条         |
| 展示方式   | 图文、纯文字、头条加列表 |
| 是否显示日期 | 是/否          |
| 是否显示摘要 | 是/否          |
| 更多链接   | 自动或手动配置      |

##### 快捷入口

| 字段   | 说明      |
| ---- | ------- |
| 入口名称 | 智慧校园    |
| 入口图标 | 图片或图标   |
| 跳转地址 | 外部系统地址  |
| 打开方式 | 当前页/新窗口 |
| 排序   | 显示顺序    |
| 是否启用 | 是/否     |

首页建议设计成可配置区块，但不要做复杂拖拽装修系统，否则开发成本会明显增加。

---

#### 14.4.2 文章列表页模板 `ARTICLE_LIST`

这个模板主要由栏目后台配置，文章内容仍使用公共内容字段。

##### 栏目配置字段

| 字段       | 类型 | 说明         |
| -------- | -- | ---------- |
| 栏目名称     | 文本 | 学校新闻、通知公告等 |
| 栏目副标题    | 文本 | 可选英文或说明    |
| 栏目Banner | 图片 | 页面顶部图片     |
| 列表样式     | 选择 | 纯文字、图文、卡片  |
| 每页数量     | 数字 | 例如10、15、20 |
| 是否显示封面图  | 开关 | 控制列表图片     |
| 是否显示摘要   | 开关 | 控制摘要       |
| 是否显示发布时间 | 开关 | 控制日期       |
| 是否显示浏览量  | 开关 | 可选         |
| 默认排序方式   | 选择 | 发布时间、排序号   |
| 空数据提示    | 文本 | 暂无相关内容     |

文章字段使用：

```text
标题
摘要
封面图
正文
来源
作者
发布时间
附件
置顶
推荐
```

---

#### 14.4.3 文章详情页模板 `ARTICLE_DETAIL`

文章详情一般不需要每个栏目单独设计，统一使用一套详情模板。

##### 详情页配置字段

| 字段          | 说明     |
| ----------- | ------ |
| 是否显示作者      | 控制前台展示 |
| 是否显示来源      | 控制前台展示 |
| 是否显示发布时间    | 控制前台展示 |
| 是否显示浏览量     | 控制前台展示 |
| 是否显示上一篇/下一篇 | 控制内容切换 |
| 是否显示附件区域    | 控制附件下载 |
| 是否显示相关推荐    | 控制推荐文章 |
| 相关推荐数量      | 例如4条   |
| 是否允许分享      | 可预留    |

内容编辑字段仍然使用公共文章字段。

---

#### 14.4.4 单页图文模板 `SINGLE_PAGE`

适合：

```text
学校简介
办学理念
历史沿革
学校领导
校园文化
联系我们
```

##### 后台字段

| 字段       | 类型    |
| -------- | ----- |
| 页面标题     | 文本    |
| 页面副标题    | 文本    |
| 页面Banner | 图片    |
| 内容摘要     | 多行文本  |
| 正文       | 富文本   |
| 主图片      | 图片    |
| 图片集      | 多图片   |
| 附件       | 多文件   |
| 联系电话     | 文本，可选 |
| 联系地址     | 文本，可选 |
| 地图地址     | 文本，可选 |
| SEO信息    | 文本    |
| 发布状态     | 状态    |

不要为“学校简介”和“办学理念”分别开发页面，它们共用单页图文模板即可。

---

#### 14.4.5 机构设置模板 `ORGANIZATION`

适合展示院系、行政部门、教学单位等。

##### 机构分类字段

| 字段   | 说明        |
| ---- | --------- |
| 分类名称 | 教学单位、行政部门 |
| 分类说明 | 可选        |
| 排序   | 显示顺序      |
| 是否显示 | 状态        |

##### 机构信息字段

| 字段     | 类型        |
| ------ | --------- |
| 机构名称   | 文本        |
| 机构简称   | 文本        |
| 机构Logo | 图片        |
| 机构简介   | 富文本或多行文本  |
| 负责人    | 文本，可选     |
| 联系电话   | 文本        |
| 办公地址   | 文本        |
| 机构网址   | 链接        |
| 跳转方式   | 内部页面/外部链接 |
| 所属分类   | 下拉选择      |
| 排序     | 数字        |
| 是否显示   | 开关        |

如果只是“子入口”，机构网址直接跳转即可，不需要每个院系建独立后台。

本期以子栏目表示机构分类，以 `cms_content` 表示机构条目，Logo、负责人、电话、地址和网址写入受控 `extension_data`，不新增机构专表。

---

#### 14.4.6 公共服务模板 `SERVICE_DIRECTORY`

适合展示：

```text
OA系统
智慧校园
校园卡
教务系统
图书馆
网上办事大厅
资料下载
```

##### 服务分类字段

| 字段   | 示例     |
| ---- | ------ |
| 分类名称 | 校园服务   |
| 分类图标 | 图标     |
| 分类说明 | 常用校园系统 |
| 排序   | 10     |

##### 服务入口字段

| 字段     | 类型           |
| ------ | ------------ |
| 服务名称   | 文本           |
| 服务图标   | 图片或图标        |
| 服务简介   | 文本           |
| 跳转地址   | URL          |
| 服务类型   | OA、校园卡、教务、其他 |
| 是否需要登录 | 开关           |
| 打开方式   | 当前页/新窗口      |
| 推荐显示   | 开关           |
| 排序     | 数字           |
| 是否启用   | 开关           |

校园网、校园卡如果只是入口，都可以使用这套后台字段。

本期以子栏目表示服务分类，以 `cms_content` 表示服务入口，图标、URL、打开方式和服务类型写入受控 `extension_data`，不新增服务专表。

---

#### 14.4.7 招生就业专题首页 `RECRUIT_HOME`

专题首页建议采用独立区块配置，但不必建设独立CMS。

##### 专题基础字段

| 字段      | 类型   |
| ------- | ---- |
| 专题名称    | 文本   |
| 专题Logo  | 图片   |
| 专题主视觉图  | 图片   |
| 手机端主视觉图 | 图片   |
| 专题简介    | 富文本  |
| 联系电话    | 文本   |
| 咨询时间    | 文本   |
| 联系地址    | 文本   |
| 招生简章链接  | 内容选择 |
| 报名入口    | 外部链接，可选 |
| 留言入口    | 外部链接，可选 |
| SEO信息   | 文本   |

##### 专题内容区块

| 区块   | 配置内容        |
| ---- | ----------- |
| 招生动态 | 选择数据栏目、展示数量 |
| 招生政策 | 选择数据栏目、展示数量 |
| 就业信息 | 选择数据栏目、展示数量 |
| 校企合作 | 选择数据栏目、展示数量 |
| 校友风采 | 选择数据栏目、展示数量 |
| 专业介绍 | 选择栏目或手动配置   |
| 快捷入口 | 图标、名称、链接    |

---

#### 14.4.8 在线报名模板（仅二期预留）

本模板不属于当前建设范围，以下字段仅用于后续变更评估，不进入本期数据库、接口、后台菜单和验收。

报名表单和普通文章不同，需要单独设计表单字段。

##### 报名活动配置

| 字段       | 说明           |
| -------- | ------------ |
| 报名名称     | 例如2026年校园开放日 |
| 报名说明     | 富文本          |
| 报名开始时间   | 日期时间         |
| 报名结束时间   | 日期时间         |
| 报名人数限制   | 数字           |
| 是否需要审核   | 开关           |
| 是否允许重复报名 | 开关           |
| 提交成功提示   | 文本           |
| 状态       | 未开始、报名中、已结束  |

##### 默认报名字段

| 字段     | 类型           |
| ------ | ------------ |
| 姓名     | 文本           |
| 性别     | 单选           |
| 手机号    | 文本           |
| 身份证号   | 文本，可根据实际需求决定 |
| 生源地区   | 省市选择         |
| 毕业学校   | 文本           |
| 意向专业   | 下拉选择         |
| 备注     | 多行文本         |
| 附件     | 文件上传，可选      |
| 隐私政策确认 | 勾选框          |

后台还需要：

```text
报名列表
条件筛选
查看详情
状态修改
备注
Excel导出
删除
统计
```

涉及身份证号、手机号时，需要做脱敏、权限和导出控制。

---

#### 14.4.9 咨询留言模板（仅二期预留）

本模板不属于当前建设范围，以下字段仅用于后续变更评估，不进入本期数据库、接口、后台菜单和验收。

##### 前台字段

| 字段   | 类型    |
| ---- | ----- |
| 咨询分类 | 下拉选择  |
| 姓名   | 文本    |
| 联系电话 | 文本    |
| 电子邮箱 | 文本，可选 |
| 咨询标题 | 文本    |
| 咨询内容 | 多行文本  |
| 附件   | 可选    |
| 验证码  | 验证码   |
| 隐私确认 | 勾选框   |

##### 后台处理字段

| 字段   | 说明              |
| ---- | --------------- |
| 处理状态 | 待处理、处理中、已回复、已关闭 |
| 回复内容 | 管理员回复           |
| 处理人  | 当前管理员           |
| 处理时间 | 自动记录            |
| 内部备注 | 只在后台显示          |
| 是否公开 | 是否在前台展示问答       |
| 提交IP | 安全日志            |
| 创建时间 | 自动记录            |

---

#### 14.4.10 搜索结果模板 `SEARCH_RESULT`

搜索页本身没有太多内容录入字段，主要是后台配置。

| 字段        | 说明              |
| --------- | --------------- |
| 搜索范围      | 新闻、通知、招生就业、所有文章 |
| 是否搜索正文    | 开关              |
| 是否搜索附件    | 基础版不建议          |
| 每页数量      | 数字              |
| 是否显示摘要    | 开关              |
| 是否显示封面图   | 开关              |
| 是否显示关键词高亮 | 开关              |
| 无结果提示     | 文本              |
| 热门关键词     | 可选              |

---

### 14.5 CMS 页面组织

不要让甲方看到一大堆技术字段，建议后台菜单这样组织：

```text
首页管理
├── 轮播图管理
├── 首页新闻区
├── 快捷入口
└── 校园风采

内容管理
├── 新闻管理
├── 通知公告
├── 学校概况
├── 教育教学
└── 学生工作

招生就业
├── 招生信息
├── 就业信息
└── 专题首页配置

页面管理
├── 栏目管理
├── 机构设置
├── 公共服务
└── 友情链接

资源管理
├── 图片库
├── 附件库
└── 视频链接

系统管理
├── 账号管理
├── 角色权限
├── 操作日志
└── 网站配置
```

甲方编辑人员通常不需要知道“模板编码”“JSON字段”等技术概念。

---

### 14.6 数据模型落地

对于你这个项目，不建议每套模板单独建一张大表。

例如不建议这样：

```text
cms_news
cms_notice
cms_school_intro
cms_teaching
cms_student
cms_admission
```

这些内容本质上都是文章，会产生大量重复表和重复代码。

#### 14.6.1 公共字段加受控扩展字段

模板元数据由后端代码中的固定注册表维护，前端根据注册表渲染编辑控件；本期不创建 `cms_template`、`cms_template_field` 等动态模板定义表。数据库只保存模板选择、经过校验的配置值和页面内容。

##### 内容表 `cms_content`

```text
id
column_id
title
subtitle
summary
cover_url
content_html
author
source
publish_at
status
top_flag
recommend_flag
sort_no
seo_title
seo_keywords
seo_description
extension_data
created_at
updated_at
```

`extension_data` 为本期新增的 JSON 字段，仅保存模板注册表允许的少量结构化扩展属性，例如：

```json
{
  "contactPhone": "0791-12345678",
  "address": "江西省南昌市",
  "gallery": [
    "/upload/image1.jpg",
    "/upload/image2.jpg"
  ]
}
```

后端必须按模板白名单校验扩展字段、数据类型和长度，禁止保存组件代码、脚本或任意 HTML。正文仍只写入 `content_html`，并按富文本安全策略处理。

##### 栏目表 `cms_column`

```text
id
parent_id
site_type
column_name
column_code
column_type
template_key
detail_template_key
template_config
route_path
external_url
cover_url
seo_title
seo_keywords
seo_description
sort_no
enabled
```

`template_key` 决定栏目页或单页模板，`detail_template_key` 决定内容详情模板，`template_config` 保存分页数量、列表样式和字段显隐等受控 JSON 配置。栏目切换模板前必须校验已有内容是否兼容。

当栏目同时具有列表和详情模板时，`template_config` 按 `page` 与 `detail` 分组，避免两套模板中 `showPublishAt` 等同名字段互相覆盖：

```json
{
  "page": {
    "listStyle": "IMAGE_TEXT",
    "pageSize": 10,
    "showPublishAt": true
  },
  "detail": {
    "showPublishAt": true,
    "showAttachments": true
  }
}
```

##### 页面区块表 `cms_page_section`

```text
id
site_type
page_code
section_code
section_name
section_type
data_source_column_id
display_count
display_style
config_json
sort_no
enabled
created_at
updated_at
created_by
updated_by
deleted
```

页面区块只允许使用 `HERO_BANNER`、`CONTENT_FEED`、`QUICK_LINKS`、`IMAGE_GALLERY`、`FRIEND_LINKS`、`CONTACT_INFO` 等预定义类型。`site_type + page_code + section_code + deleted` 保持唯一，`data_source_column_id` 必须引用同一站点栏目，`config_json` 必须通过对应区块类型的结构校验。

现有 `cms_banner` 继续负责轮播图明细，`cms_page_section` 只保存区块编排和数据来源，避免在 JSON 中重复保存文章、Banner 或媒体实体。

页面与区块矩阵由后端代码注册表固定：

| 页面编码 | 站点 | 固定区块 |
| --- | --- | --- |
| `HOME` | `MAIN_SITE` | `HERO`、`SCHOOL_NEWS`、`NOTICE`、`QUICK_LINKS`、`CAMPUS_GALLERY`、`FRIEND_LINKS` |
| `RECRUIT_HOME` | `RECRUIT_SITE` | `HERO`、`ADMISSION_NEWS`、`EMPLOYMENT_NEWS`、`SCHOOL_ENTERPRISE`、`RECRUIT_POLICY`、`QUICK_LINKS`、`CONTACT` |

`PUT /api/admin/pages/{pageCode}/sections` 接收当前页面的完整区块列表，必须包含全部预定义区块且编码、类型不可修改；允许调整区块名称、数据栏目、展示数量、预设样式、排序、启停和白名单配置。MySQL 实现在单个事务内按稳定区块编码更新或补插，local 实现使用同一整页保存语义。本期不提供区块新增、删除、自由拖拽组件或任意组件嵌套。

区块约束如下：

| 区块类型 | 数据栏目 | 展示数量 | 预设样式 |
| --- | --- | --- | --- |
| `HERO_BANNER` | 禁止 | 禁止 | `FULL_WIDTH` |
| `CONTENT_FEED` | 必须，同站点且启用 | 必须，1-20 | `TEXT_LIST`、`IMAGE_TEXT`、`CARD` |
| `QUICK_LINKS` | 禁止 | 可选，1-30 | `ICON_GRID`、`TEXT_LIST` |
| `IMAGE_GALLERY` | 禁止 | 可选，1-20 | `GRID`、`CAROUSEL` |
| `FRIEND_LINKS` | 禁止 | 可选，1-50 | `TEXT_LINKS`、`LOGO_GRID` |
| `CONTACT_INFO` | 禁止 | 禁止 | `DEFAULT`、`COMPACT` |

`config_json` 白名单按区块类型固定：主视觉允许 `bannerPosition`、`autoplay`、`intervalSeconds`；内容流允许 `showSummary`、`showCover`、`moreLinkText`；快捷入口允许 `showDescription`、`maxRows`；图片区允许 `aspectRatio`、`showCaption`；友情链接允许 `showLogo`、`openInNewWindow`；联系信息允许 `showPhone`、`showEmail`、`showAddress`。未知字段、错误类型、越界数字、未允许选项和 HTML 均拒绝保存。

页面编码唯一确定站点，客户端不能另传 `site_type`。被未删除页面区块引用的栏目禁止通过完整编辑或状态接口停用，也禁止删除。读取页面区块复用 `cms:column` 权限，整页保存复用 `cms:column:manage` 权限，不为轻量化页面配置新增独立权限菜单。

##### Banner 模板增量

为支持桌面/移动主视觉和内部内容选择，`cms_banner` 在模板增量中增加 `subtitle`、`mobile_image_url`、`link_type` 和 `link_ref_id`。`link_type` 只允许 `NONE`、`CONTENT`、`COLUMN`、`EXTERNAL`；内部跳转使用引用 ID，外部跳转使用现有 `link_url`，保存时校验互斥关系和站点归属。已有非空 `link_url` 在升级时迁移为 `EXTERNAL`，并通过 `link_type + link_ref_id + deleted` 索引支持内部引用占用检查。

#### 14.6.2 SQL 交付与升级路径

新环境执行 `src/main/resources/db/init.sql` 获取当前完整结构和初始化数据；已运行旧版初始化脚本的环境，按日期顺序执行 `src/main/resources/db/migration` 中的增量 SQL。本项目当前不引入 Flyway 或 Liquibase，生产变更由部署人员备份后手工执行并记录。

`20260717_page_template_schema.sql` 使用 `information_schema` 判断列和索引是否存在，可在测试环境重复执行。脚本只为 `NULL` 的 `template_config` 填充默认值，页面区块冲突更新时保留已经维护的数据源、数量、样式和配置，避免升级覆盖运营数据。

##### 二期表单模型（本期不实施）

```text
cms_form
cms_form_field
cms_form_submission
cms_form_submission_value
```

上述表单表仅作为在线报名、咨询留言的二期建模方向，本期初始化 SQL 不创建这些表。

### 14.7 编辑字段与接口契约

后台不直接读取数据库 JSON 猜测表单，而是先取得模板注册表和栏目生效配置，再渲染对应编辑器。建议接口如下：

```text
GET /api/admin/page-templates
GET /api/admin/columns/{id}/editor-schema
GET /api/admin/pages/{pageCode}/sections
PUT /api/admin/pages/{pageCode}/sections
GET /api/portal/pages/{pageCode}
```

`editor-schema` 返回公共字段、模板扩展字段、必填规则、控件类型和只读状态；内容保存接口仍接收结构化 DTO，并由后端再次校验，不能把前端 schema 当作安全边界。公开页面接口只返回已启用栏目、已发布内容和当前时间范围内有效的 Banner。

模板切换规则：

1. 无内容栏目可直接切换兼容模板。
2. 已有内容栏目切换前执行兼容性检查，并提示将被隐藏或丢弃的配置项。
3. `MAIN_SITE` 与 `RECRUIT_SITE` 的栏目、内容和区块引用不得跨站点混用。
4. 删除或禁用数据源栏目时，必须提示受影响区块并阻止产生悬空引用。
5. Banner 内部引用同样不得跨站点；内容下线、栏目停用和双方删除前必须执行 Banner 占用检查。

#### 14.7.1 字段控件类型

模板注册表中的字段描述统一使用以下控件类型。本期控件集合由代码固定，不提供数据库动态新增控件能力。

```text
TEXT            单行文本
TEXTAREA        多行文本
RICH_TEXT       富文本
NUMBER          数字
DATE            日期
DATETIME        日期时间
SELECT          下拉选择
RADIO           单选
CHECKBOX         多选
SWITCH          开关
IMAGE           单图片
IMAGE_LIST      多图片
FILE            单文件
FILE_LIST       多文件
LINK            链接
COLUMN_SELECT   栏目选择
CONTENT_SELECT  内容选择
```

每个字段定义应包含：

| 配置             | 说明        |
| -------------- | --------- |
| fieldCode      | 字段编码      |
| fieldName      | 后台显示名称    |
| fieldType      | 字段类型      |
| required       | 是否必填      |
| defaultValue   | 默认值       |
| placeholder    | 输入提示      |
| validationRule | 校验规则      |
| options        | 下拉或单选选项   |
| sort           | 表单中的顺序    |
| enabled        | 是否启用      |
| readOnly       | 是否只读      |
| helpText       | 给编辑人员看的说明 |

#### 14.7.2 固定模板注册表契约

`GET /api/admin/page-templates` 要求 `cms:column` 权限，按固定顺序返回八类模板。每项包含 `templateKey`、`templateName`、`description`、`usage`、`compatibleSiteTypes`、`compatibleColumnTypes`、`defaultDetailTemplateKey` 和 `editorSchema`，不接受管理员新增或修改模板定义。

`editorSchema` 按存储边界拆分为以下字段组：

| 字段组 | 用途 | 后续保存位置 |
| --- | --- | --- |
| `columnFields` | 栏目级样式、分页和显隐配置 | `cms_column` 固定列或 `template_config` |
| `contentFields` | 标题、正文、发布状态等公共内容 | `cms_content` 和附件关系 |
| `extensionFields` | 机构、服务和单页的受控扩展属性 | `cms_content.extension_data` |
| `pageFields` | 首页、专题首页和搜索页的页面级配置 | 站点配置、页面配置及预定义区块 |

模板兼容矩阵固定如下：

| 模板编码 | 用途 | 适用站点 | 兼容栏目类型 | 默认详情模板 |
| --- | --- | --- | --- | --- |
| `HOME` | `LANDING` | `MAIN_SITE` | `SPECIAL` | 无 |
| `ARTICLE_LIST` | `COLUMN` | 两类站点 | `LIST`、`IMAGE`、`DOWNLOAD` | `ARTICLE_DETAIL` |
| `ARTICLE_DETAIL` | `DETAIL` | 两类站点 | `LIST`、`IMAGE`、`DOWNLOAD` | 无 |
| `SINGLE_PAGE` | `COLUMN` | 两类站点 | `PAGE` | 无 |
| `ORGANIZATION` | `COLUMN` | `MAIN_SITE` | `LIST` | 无 |
| `SERVICE_DIRECTORY` | `COLUMN` | `MAIN_SITE` | `LIST` | 无 |
| `RECRUIT_HOME` | `LANDING` | `RECRUIT_SITE` | `SPECIAL` | 无 |
| `SEARCH_RESULT` | `SYSTEM` | 两类站点 | 不绑定栏目 | 无 |

`LINK` 栏目直接使用 `external_url` 跳转，不绑定页面模板。模板注册表提供的校验规则和选项也是后端保存校验的输入，前端隐藏字段或绕过控件不能绕过服务端白名单。

#### 14.7.3 公开页面聚合契约

主站首页使用 `GET /api/portal/pages/HOME`，招生就业专题首页可使用 `GET /api/portal/pages/RECRUIT_HOME` 或兼容入口 `GET /api/portal/recruit/home`。两个接口均允许匿名访问，页面编码由后端注册表唯一映射到站点和生效模板，不接受客户端另传 `siteType` 或模板编码。

页面响应根对象固定包含：

| 字段 | 说明 |
| --- | --- |
| `pageCode` | `HOME` 或 `RECRUIT_HOME` |
| `templateKey` | 页面当前生效模板，与页面注册表一致 |
| `siteType` | 页面所属站点 |
| `siteConfig` | `GLOBAL` 与当前站点配置合并结果，站点配置覆盖同名全局配置 |
| `seo` | `SeoMetadataService` 解析的页面默认 SEO 和 canonical 路径 |
| `sections` | 按 `sortNo`、`id` 排序的已启用区块 |

每个区块固定返回 `sectionCode`、`sectionName`、`sectionType`、`sourceColumn`、`displayCount`、`displayStyle`、`config`、`sortNo`，并同时提供 `banners`、`contents`、`links`、`friendLinks` 和 `contact` 五类稳定数据槽。当前区块未使用的数据槽返回空数组或空对象，不返回 `null`，前端只需按 `sectionType` 选择对应数据槽。

区块数据来源固定如下：

| 区块类型 | 聚合来源与公开过滤 |
| --- | --- |
| `HERO_BANNER` | 页面同名 Banner 位；只返回已启用且当前有效，并且内部内容或栏目引用仍可公开访问的数据 |
| `CONTENT_FEED` | `dataSourceColumnId` 指定的同站点启用栏目；只返回发布时间不晚于当前时间的 `PUBLISHED` 内容 |
| `QUICK_LINKS` | 当前站点已启用且 `navVisible = true` 的栏目，按栏目排序并应用展示数量 |
| `IMAGE_GALLERY` | 当前站点已发布、已推荐且配置了封面的内容，按内容公开排序并应用展示数量 |
| `FRIEND_LINKS` | `GLOBAL` 与当前站点作用域内已启用的友情链接 |
| `CONTACT_INFO` | 合并后的站点配置；按 `showPhone`、`showEmail`、`showAddress` 控制返回字段 |

若内容流的数据源栏目不存在、已停用或站点不匹配，聚合接口直接剔除该区块，避免把悬空配置暴露给前端。合法区块没有数据时仍保留区块元数据并返回空数据槽，保证空数据响应结构稳定。

#### 14.7.4 公开站点公共数据契约

站点配置、导航、Banner 和友情链接独立接口与页面聚合共用 `PortalSiteService`，避免同一数据在独立接口和聚合接口中使用不同公开规则。四个接口均为匿名 GET，并要求显式传入目标 `siteType`。

| 接口 | 响应与过滤规则 |
| --- | --- |
| `GET /api/portal/site-config` | 返回 `siteType` 和 `configs`；先加载 `GLOBAL`，再由当前站点同名键覆盖 |
| `GET /api/portal/navigation` | 只返回同站点、已启用且导航可见的栏目树，不暴露 SEO 原始字段、备注、审计时间或后台状态字段 |
| `GET /api/portal/banners` | 只返回同站点、指定位置、已启用且当前有效的 Banner，并过滤不可公开的内部内容或栏目引用 |
| `GET /api/portal/friend-links` | 合并 `GLOBAL` 与当前站点作用域内已启用链接，只返回名称、地址和 Logo |

导航节点固定返回 `id`、`parentId`、`name`、`code`、`columnType`、`routePath`、`externalUrl`、`templateKey`、`detailTemplateKey`、`coverUrl` 和 `children`。正常父子栏目保持树结构；若历史数据中父栏目已隐藏而子栏目仍可见，公开导航将该子栏目提升为根节点，避免可见入口被静默丢弃。兄弟节点按 `sortNo`、`id` 升序返回。

`HOME` 位置只允许 `MAIN_SITE`，`RECRUIT_HOME` 位置只允许 `RECRUIT_SITE`，`COLUMN` 可用于两类站点。独立 Banner 查询和页面主视觉聚合复用相同当前时间和内部引用校验，禁止出现一个接口可见、另一个接口不可见的状态漂移。

#### 14.7.5 公开栏目与内容契约

公开栏目与内容接口均允许匿名 GET。栏目树显式传入 `siteType`，栏目详情、栏目内容分页和内容详情使用全局唯一 ID 定位数据，并从目标栏目或内容本身确定站点，不通过域名、Referer 或客户端附加字段推断站点。

| 接口 | 响应与过滤规则 |
| --- | --- |
| `GET /api/portal/columns` | 返回指定站点全部已启用栏目树，不受 `navVisible` 限制；节点结构与公开导航节点一致 |
| `GET /api/portal/columns/{id}` | 返回已启用栏目公开详情、受控模板配置和解析后的 SEO 数据 |
| `GET /api/portal/columns/{id}/contents` | 返回当前栏目已发布且已到发布时间的内容分页，不返回草稿、下线或未来定时内容 |
| `GET /api/portal/contents/{id}` | 返回可公开内容正文、扩展数据、附件和解析后的 SEO 数据 |

完整栏目树节点固定返回 `id`、`parentId`、`name`、`code`、`columnType`、`routePath`、`externalUrl`、`templateKey`、`detailTemplateKey`、`coverUrl` 和 `children`。已停用栏目不出现在树中；若已启用子栏目的父栏目缺失或停用，该子栏目提升为根节点。兄弟节点按栏目 `sortNo`、`id` 升序返回。

栏目详情在树节点字段基础上增加 `siteType`、`templateConfig` 和 `seo`，不返回 `enabled`、`navVisible`、原始 SEO 字段、备注或审计时间。不存在或已停用栏目统一返回 HTTP 404，避免向匿名调用方暴露后台状态差异。

栏目内容分页使用 `pageNo` 和 `pageSize`，默认分别为 `1` 和 `10`，`pageSize` 允许范围为 `1-100`。总数统计和分页查询在相同公开条件下执行，不允许先对后台结果分页再在内存过滤。排序固定为置顶优先、`sortNo` 升序、`publishAt` 降序、`id` 降序。列表项返回 `id`、栏目标识与名称、标题、副标题、摘要、封面、来源、作者、发布时间、置顶/推荐标记和浏览量。

内容详情固定返回 `id`、栏目标识与名称、`siteType`、标题、副标题、摘要、已清洗正文、封面、来源、作者、发布时间、浏览量、受控 `extensionData`、`attachments` 和 `seo`。附件只返回 `id`、文件名、公开地址、大小、类型和排序，不返回内部 `mediaId`、内容关联 ID 或审计时间。内容仅在所属栏目已启用、状态为 `PUBLISHED`、`publishAt` 非空且不晚于服务器当前时间时可访问；其余情况统一返回 HTTP 404。

栏目详情和内容详情在完成公开状态校验后复用 `SeoMetadataService`，按 5.7 节规则返回 `title`、`keywords`、`description` 和 `canonicalPath`。本轮列表与详情直接查询 local/MySQL 仓储，不启用结果缓存，后台发布、下线或栏目停用后立即影响公开结果。

#### 14.7.6 公开搜索、浏览量与时间边界契约

`GET /api/portal/search` 允许匿名访问，查询参数如下：

| 参数 | 规则 |
| --- | --- |
| `keyword` | 必填，去除首尾空白后按内容标题包含匹配，最长 100 个字符 |
| `siteType` | 必填，只接受 `MAIN_SITE` 或 `RECRUIT_SITE` |
| `columnId` | 可选；填写时栏目必须已启用且属于目标站点，否则返回 HTTP 404 |
| `pageNo` | 可选，默认 `1`，最小 `1` |
| `pageSize` | 可选，默认 `10`，范围 `1-100` |

搜索计数和分页查询使用同一组条件：栏目已启用且与目标站点一致，内容状态为 `PUBLISHED`，`publishAt` 非空且不晚于服务器当前时间，标题包含关键词，并在指定时限制栏目 ID。结果按 `publishAt`、`id` 降序返回；首版不搜索副标题、摘要、正文或附件，不执行高亮、分词、相关度评分、热门搜索或搜索日志统计。

搜索响应固定返回 `keyword`、`siteType`、`columnId`、`seo`、`records`、`total`、`pageNo` 和 `pageSize`。`records` 复用公开内容摘要字段；空结果返回空数组及 `total = 0`。`seo` 复用 `SeoMetadataService.resolvePage(siteType, "/search")`，标题、关键词和描述回退到目标站点默认 SEO，canonical 路径固定为 `/search`，查询字符串不进入 canonical。

`PUT /api/portal/contents/{id}/view-count` 是唯一允许匿名调用的 Portal 写接口。MySQL 使用带公开状态条件的原子 `view_count = view_count + 1`，local 实现使用同步更新；只有所属栏目启用、内容已发布且已到发布时间时才能递增，否则统一返回 HTTP 404。成功响应返回内容 `id` 和递增后的 `viewCount`。本期浏览量是轻量展示计数，不按 IP、设备、会话或时间窗口去重，不用于复杂 BI 或精确流量审计。

所有公开时间判断使用服务器当前时间并采用包含边界：内容满足 `publishAt <= now` 时生效；Banner 满足 `startTime` 为空或 `startTime <= now`，同时 `endTime` 为空或 `endTime >= now` 时生效。local 与 MySQL 必须保持相同判断，不允许通过先分页后过滤造成 total、records 或公开状态不一致。

#### 14.7.7 Portal DTO、OpenAPI 与空数据契约

Portal API 使用独立响应 DTO，不直接返回后台栏目、内容、Banner 或友情链接实体。`PortalPageResponse`、`PortalPageSectionResponse`、`PortalSiteConfigResponse`、`PortalColumnTreeNodeResponse`、`PortalColumnDetailResponse`、`PortalContentSummaryResponse`、`PortalContentDetailResponse`、`PortalSearchResponse` 和 `PortalViewCountResponse` 均在 OpenAPI 组件中提供用途说明和可解析 JSON 示例。

查询参数在 OpenAPI 中明确提供说明、示例、默认值和边界。`PortalContentPageQuery` 与 `PortalSearchQuery` 使用参数对象展开，生成文档必须显示独立的 `keyword`、`siteType`、`columnId`、`pageNo` 和 `pageSize`，不能折叠为含义不明的单个 `query` 参数。页面编码、站点、Banner 位置、栏目 ID 和内容 ID 同样提供调用示例。

空数据结构固定如下：

| 场景 | 响应规则 |
| --- | --- |
| 普通集合 | 返回 `[]`，不返回 `null` |
| 分页列表与搜索 | 返回 `records: []`、真实 `total`、`pageNo` 和 `pageSize` |
| 页面区块 | `banners`、`contents`、`links`、`friendLinks` 固定为数组；`config`、`contact` 固定为对象 |
| 栏目树 | 每个节点固定返回 `children` 数组，叶子节点为 `[]` |
| 站点配置与 SEO | 合法请求返回对象；缺失可选值使用空字符串、空对象或字段级 `null` 契约，不省略顶层对象 |

自动化测试直接解析 `/v3/api-docs`，校验全部 Portal 路径、HTTP 方法、搜索参数、分页默认值/上限和关键 DTO 示例。空数据测试遍历页面区块和栏目树，保证集合及对象槽稳定。

当前实现不启用服务端结果缓存，因此所谓“缓存失效”按即时一致性契约验证：后台修改站点配置，以及新增、停用导航栏目、Banner、友情链接后，紧随其后的 Portal 请求必须读取最新状态。未来启用 Redis 后应保留同一组 HTTP 测试，并在其下补充缓存 Key 删除和 TTL 测试。

### 14.8 SEO 数据与接口输出规则

1. Portal API 复用 `SeoMetadataService`，按 5.7 节规则计算并返回 SEO 回退结果，不在控制器中重复实现。
2. 已下线、已删除或不存在内容返回正确 API HTTP 状态，不以空对象冒充有效数据；canonical 路径所需标识由接口返回，最终标签由前端渲染。
3. Banner、封面和主视觉可分别保存桌面端与移动端资源地址；未配置移动图时的裁切和展示策略由前端负责。
4. Portal 列表接口通过明确的查询参数接收分页、筛选和排序条件，并在 OpenAPI 中说明默认值和边界。
5. 页面区块配置修改后主动失效对应聚合缓存；Portal API 不返回草稿、下线内容或过期 Banner。
6. 图片替代文本、标题层级、语义结构、键盘访问、响应式和浏览器兼容由前端实现；后端只提供约定字段。

### 14.9 后端与前端责任映射

| 能力 | 后端专项责任 | 外部前端团队责任 |
| --- | --- | --- |
| 模板 | 固定八类模板元数据、schema 和校验 | 按模板实现编辑控件与页面组件 |
| 首页区块 | 保存、校验、排序并聚合结构化配置 | 布局、样式、动效与交互 |
| 内容 | 状态流转、富文本清洗、附件和公开过滤 | 编辑器集成与内容展示 |
| SEO | 返回标题、关键词、描述和 canonical 数据 | 渲染标签、语义结构、SSR 或静态化 |
| 媒体 | 上传、校验、元数据和访问地址 | 图片裁切、懒加载和展示降级 |
| 安全 | JWT、权限、参数、XSS、URL 和引用校验 | 令牌保存、前端路由守卫和输出编码 |
| 测试 | 单元、仓储、接口、权限和安全测试 | 页面、响应式、兼容性、视觉和前端性能测试 |
| 部署 | Java 服务、MySQL、上传目录和 API 代理 | 前端构建、静态资源、页面域名和前端监控 |

### 14.10 核心设计原则

**栏目和模板分开，模板和内容分开，公共字段和个性字段分开。**

推荐关系：

```text
栏目
  ↓ 选择
页面模板
  ↓ 决定
后台编辑字段
  ↓ 保存
文章内容或页面区块
  ↓
Portal API 输出结构化数据
  ↓
外部前端按模板展示
```

方案表述统一为：

> 后端管理字段根据页面模板分类设计，相同类型页面共用统一字段和数据契约。新闻、通知、招生信息等内容共用文章数据模型；首页和招生就业专题首页采用受控区块配置。后端交付模板元数据、校验和聚合 API，不交付 CMS、官网或专题站前端代码。新增不同数据结构或接口聚合模型须另行评估，在线报名、咨询留言和自由页面装修不属于本期后端范围。
