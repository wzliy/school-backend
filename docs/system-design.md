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

栏目是官网导航、内容聚合和专题站结构的基础。栏目支持树形结构、排序、启用禁用、导航显示控制、SEO 配置和站点类型区分。

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

内容管理支持文章维护、草稿、发布、下线、置顶、推荐、封面图、附件、浏览量和 SEO 信息。内容归属于栏目，通过栏目站点类型区分主站和招生就业专题站内容。

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
```

招生就业专题站首版优先复用 `cms_column` 和 `cms_content`，通过 `site_type = RECRUIT_SITE` 区分，避免重复建模。

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

栏目管理：

```text
GET    /api/admin/columns/tree
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
6. 上线前执行数据库初始化脚本、部署脚本和回滚方案验证。

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
