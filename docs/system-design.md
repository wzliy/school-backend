# 高校官网建设项目系统设计文档

## 1. 项目概述

高校官网建设项目按轻量化方式建设，面向一个官网主站、一个招生就业专题站和一套 CMS 后台管理场景。系统聚焦官网内容发布、栏目管理、Banner 管理、媒体库、用户角色权限、站点配置、基础 SEO、搜索和前台展示能力。

项目采用前后端分离和单体模块化后端，不建设复杂站群系统。主站与招生就业专题站共用 CMS、后端服务和内容模型，通过站点类型隔离栏目、内容及 Banner 数据。

本期明确不建设多语言、短信、邮件、支付、复杂 BI、深度智慧校园集成、复杂多级审核、商业全文检索和视频转码。此类需求不属于当前报价，后续提出时须重新评估架构、费用、工期和验收标准。

## 2. 建设目标

1. 支持高校主站动态内容展示，包括首页、栏目页、内容详情页、搜索页和公共服务入口。
2. 支持招生就业专题站独立展示，具备独立栏目、Banner 和内容维护能力。
3. 支持 CMS 后台管理，包括登录认证、用户角色权限、栏目、内容、Banner、媒体库、站点配置和日志管理。
4. 提供标准 RESTful API，统一返回格式、错误码、鉴权和参数校验。
5. 支持 MySQL 数据持久化和本地文件存储；Redis 与 MinIO 根据实际部署需要选择启用。
6. 支持 PC 和移动端响应式访问，满足高校官网常见信息发布和内容运营需求。
7. 控制系统复杂度，以单套部署满足轻量官网的交付、运维和三年维护需要。

## 3. 技术架构

### 3.1 总体架构

```text
用户浏览器
├── 官网前台 portal-web
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
└── Nginx
    ├── 静态资源托管
    ├── API 反向代理
    └── 文件访问代理

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
| 后台前端 | Vue 3 + TypeScript + Vite + Element Plus |
| 官网前台 | Vue 3 + TypeScript + Vite |
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

### 4.2 后台管理端

后台管理端面向管理员、网站管理员、内容编辑和招生就业管理员。功能包括内容运营、系统配置、账号权限和日志查看。首版采用简化发布流程，不建设跨部门多级审核工作流。

首版页面范围：

```text
登录页
后台首页
首页区块管理
用户管理
角色管理
权限管理
栏目管理
内容管理
Banner 管理
媒体库管理
友情链接管理
站点配置
操作日志
登录日志
```

栏目编辑时选择固定页面模板，内容编辑页根据模板注册表只展示相关字段。首页和招生就业专题首页使用预定义区块管理，不向编辑人员暴露模板编码、JSON 或组件实现细节。

### 4.3 官网前台

官网前台面向公众访问，提供主站和招生就业专题站内容展示。前台 API 原则上只暴露已启用、已发布、未删除的数据。

首版页面范围：

```text
主站首页
新闻中心
栏目列表页
内容详情页
学校概况单页
机构设置
教育教学
学生工作
公共服务
搜索页
招生就业专题站首页
招生就业内容列表页
招生就业详情页
```

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

### 5.4 Banner 管理

Banner 支持主站首页、招生就业专题站和栏目页展示位置。Banner 可配置图片、跳转链接、排序、启用状态和展示时间范围。

展示位置：

```text
HOME
RECRUIT_HOME
COLUMN
```

### 5.5 媒体库

媒体库负责图片、文档、视频和其他文件的上传、查询、预览和删除。首版实现本地文件存储，抽象存储接口并预留 MinIO 实现。

文件类型：

```text
IMAGE
DOCUMENT
VIDEO
OTHER
```

### 5.6 站点配置与友情链接

站点配置用于维护网站名称、Logo、备案号、联系电话、联系地址、版权信息、首页展示数量和默认 SEO 信息。

友情链接支持名称、链接地址、Logo、排序和启用禁用。

### 5.7 搜索

首版搜索基于数据库查询实现，支持关键词匹配标题、按栏目筛选、发布时间排序和分页展示。后续如需全文检索，可接入 Elasticsearch 或 MySQL Fulltext。

### 5.8 日志

日志分为登录日志、操作日志、内容发布日志、文件上传日志和接口异常日志。首版至少落库登录日志和后台写操作日志。

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

页面区块管理：

```text
GET /api/admin/pages/{pageCode}/sections
PUT /api/admin/pages/{pageCode}/sections
```

前台公开 API：

```text
GET /api/portal/site-config
GET /api/portal/navigation
GET /api/portal/banners
GET /api/portal/columns/{id}
GET /api/portal/columns/{id}/contents
GET /api/portal/contents/{id}
PUT /api/portal/contents/{id}/view-count
GET /api/portal/search
GET /api/portal/friend-links
GET /api/portal/recruit/home
GET /api/portal/pages/{pageCode}
```

## 8. 安全设计

1. 后台接口使用 Spring Security OAuth2 Resource Server + JWT 鉴权，JWT 签发与校验统一使用 Spring `JwtEncoder`、`JwtDecoder`，不直接依赖 JJWT。
2. 密码使用 BCrypt 加密存储。若使用 `DelegatingPasswordEncoder`，密码字段统一保存 `{bcrypt}` 前缀；升级前已有的无前缀 BCrypt 数据须迁移或配置明确的兼容编码器。
3. 未登录访问后台接口返回 401。
4. 无权限访问后台接口返回 403。
5. 文件上传限制大小、后缀和 MIME 类型。
6. 富文本内容进入前台展示前进行安全处理，避免 XSS。
7. 后台写操作记录操作日志，包含用户、IP、接口、参数摘要和执行结果。
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

首版缓存以简单、可失效为原则：

| 缓存对象 | Key 示例 | 失效策略 |
| --- | --- | --- |
| 站点配置 | `site:config` | 修改配置后删除 |
| 导航栏目 | `site:navigation:{siteType}` | 修改栏目后删除 |
| 首页 Banner | `site:banner:{position}` | 修改 Banner 后删除 |
| 友情链接 | `site:friend-links` | 修改友情链接后删除 |

内容详情和列表首版可直接查库，待访问量上升后再增加缓存。

## 10. 文件存储设计

定义统一存储接口：

```text
StorageService
├── LocalStorageService
└── MinioStorageService
```

配置示例：

```yaml
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
    - pdf
    - doc
    - docx
    - xls
    - xlsx
```

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
├── /              portal-web 静态资源
├── /admin         admin-web 静态资源
├── /api           school-backend 反向代理
└── /uploads       文件访问

school-backend (Java 25)
├── MySQL
├── Local Storage
└── Redis / MinIO（按需启用）
```

## 12. 质量保障

1. 单元测试覆盖统一返回、异常处理、核心 Service 和权限判断。
2. 接口测试覆盖认证、栏目、内容、Banner、媒体库和前台公开 API。
3. 权限测试覆盖未登录、无权限、不同角色访问边界。
4. 文件上传测试覆盖大小限制、后缀限制、访问地址和删除逻辑。
5. 前台测试覆盖首页、栏目页、详情页、搜索页和移动端响应式。
6. 模板测试覆盖八类模板的字段显隐、必填校验、数据来源、空状态、SEO 回退和桌面/移动端展示。
7. 页面区块测试覆盖排序、启停、栏目引用、跨站点拦截、配置校验和缓存失效。
8. 上线前执行数据库初始化脚本、模板增量 SQL、部署脚本和回滚方案验证。

## 13. 范围约束与变更原则

当前架构和报价只覆盖官网主站、招生就业专题站、CMS 后台、内容运营、基础 SEO、接口联调、测试上线、域名绑定和三年维护。

以下能力不在本期设计范围内：

```text
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

## 14. 页面模板设计

页面体系采用“**栏目选择模板，模板决定编辑字段，前台按模板渲染**”的方式。模板是受控的业务类型，不是可由管理员上传代码或任意拖拽生成页面的低代码能力。

也就是说，后台新增栏目时先选择页面类型：

```text
学校新闻 → 新闻列表模板
学校简介 → 单页图文模板
机构设置 → 机构展示模板
公共服务 → 服务入口模板
招生就业 → 招生就业专题模板
```

编辑人员进入对应栏目后，后台只展示该模板需要填写的字段，不要把所有字段一次性都放出来。

### 14.1 本期模板基线

42.8 万元标准方案交付八类固定模板；38.8 万元精简方案沿用同一数据模型，但减少可选样式和区块配置项。

| 模板编码 | 模板名称 | 适用场景 | 主要数据来源 | 本期范围 |
| --- | --- | --- | --- | --- |
| `HOME` | 官网首页 | 主站首页 | 站点配置、Banner、页面区块 | 标准与精简方案 |
| `ARTICLE_LIST` | 文章列表页 | 新闻、通知、招生信息 | 栏目、内容分页 | 标准与精简方案 |
| `ARTICLE_DETAIL` | 文章详情页 | 新闻和普通文章详情 | 内容、附件 | 标准与精简方案 |
| `SINGLE_PAGE` | 单页图文页 | 学校简介、办学理念、联系我们 | 单条内容、附件 | 标准与精简方案 |
| `ORGANIZATION` | 机构展示页 | 院系、行政部门、教学单位 | 栏目及结构化内容 | 标准方案；精简方案使用简单列表样式 |
| `SERVICE_DIRECTORY` | 公共服务页 | OA、图书馆、校园服务入口 | 分类及结构化内容 | 标准方案；精简方案使用简单列表样式 |
| `RECRUIT_HOME` | 招生就业专题首页 | 招生就业专题入口 | 专题配置、Banner、页面区块 | 标准与精简方案 |
| `SEARCH_RESULT` | 搜索结果页 | 全站文章搜索 | 搜索 API | 标准与精简方案 |

在线报名和咨询留言可继续保留为二期模板设计参考，但涉及公众数据采集、验证码、隐私授权、脱敏、导出和审核，本期不建表、不开发接口、不进入 42.8 万元或 38.8 万元验收范围。专题中的“报名入口”“留言入口”本期仅支持配置外部链接。

本期模板边界如下：

1. 模板编码由代码和版本管理固定维护，管理员不能新增可执行模板。
2. 首页和专题首页允许配置预定义区块的标题、数据来源、数量、样式、排序和启停，不支持自由拖拽、任意嵌套或上传组件。
3. 普通栏目复用文章列表、详情和单页模板；新增同结构栏目不新增前端页面开发量。
4. 新增不同数据结构、特殊交互或独立视觉模板时，须按变更流程评估工期和费用。

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

### 14.8 SEO、响应式与渲染规则

1. 页面标题按“内容 SEO 标题 > 栏目 SEO 标题 > 内容标题或栏目名称 > 站点默认标题”顺序回退，关键词和描述采用同类回退规则。
2. 每个模板输出稳定的语义结构和 canonical 地址；已下线、已删除或不存在内容返回正确 HTTP 状态，不以空白页面代替。
3. Banner、封面和主视觉支持桌面端与移动端资源；未单独配置移动图时使用桌面图的安全裁切规则。
4. 列表页的分页、筛选和排序参数必须进入明确的 URL 查询参数，禁止把内容分页状态只保存在前端内存中。
5. 模板需在约定桌面、平板和手机视口下验证导航、标题、图片比例、表格、附件和长文本，不允许横向溢出或内容遮挡。
6. 页面区块配置修改后主动失效对应页面缓存；前台不得长期缓存草稿、下线内容或过期 Banner。
7. 图片必须提供替代文本配置，交互控件支持键盘访问，标题层级在同一模板内保持连续。

### 14.9 标准与精简方案映射

| 能力 | 42.8 万元标准方案 | 约 38.8 万元精简方案 |
| --- | --- | --- |
| 模板范围 | 交付八类模板及约定视觉样式 | 交付相同核心模板，机构与公共服务采用简化样式 |
| 首页区块 | 可配置标题、数据栏目、数量、预设样式、排序和启停 | 固定区块顺序，开放必要的数据栏目和数量配置 |
| 专题首页 | 独立主视觉及预定义专题区块 | 最大化复用主站区块和列表组件 |
| 列表样式 | 在约定的纯文字、图文等预设样式中选择 | 每类栏目使用默认样式 |
| 字段显隐 | 支持约定范围内的显示开关 | 使用模板默认字段组合 |
| 媒体使用 | 支持媒体库分类、选择和复用 | 保留基础上传和选择 |
| 范围外能力 | 不含自由拖拽、在线报名、咨询留言和动态表单 | 同标准方案 |

两种方案都使用固定模板注册表和相同安全校验，不通过删除权限、SEO 基础能力、响应式验证或数据校验降低报价。

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
前台按模板展示
```

方案表述统一为：

> 后台管理字段根据页面模板分类设计，相同类型页面共用统一字段和模板。新闻、通知、招生信息等内容共用文章数据模型；首页和招生就业专题首页采用受控区块配置。新增同结构普通栏目不产生额外模板开发费用，新增不同数据结构、特殊交互或独立视觉模板须另行评估费用。在线报名、咨询留言和自由页面装修不属于本期建设范围。
