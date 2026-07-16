下面这份可以直接作为 **Codex 项目规划 Prompt** 使用。它不是简单功能说明，而是把你现有功能清单拆成了：**项目目标、技术架构、模块规划、数据库设计方向、接口规划、开发阶段、验收标准、Codex 开发任务**。

你可以复制到 Codex 中，让它按这个规划开始生成项目代码。

---

# 高校官网建设项目开发规划

## 一、项目定位

本项目为高校官网建设项目，建设内容包括：

1. 官网前台展示系统；
2. 招生就业专题站；
3. CMS 后台管理系统；
4. 前后端 RESTful API；
5. 内容发布、栏目管理、Banner 管理、媒体库管理；
6. 账号权限管理；
7. SEO、响应式适配、测试与上线保障。

项目不是简单静态展示站，而是一个具备内容管理能力的高校官网系统。现有功能清单中已经明确包含 UI/UX 设计、官网前台开发、招生就业专题站、CMS 后台、接口联调、测试与质量保障等内容。

本项目按轻量化方式建设，固定为一个官网主站、一个招生就业专题站和一套 CMS 后台，不建设复杂站群系统。标准整包报价建议为 **42.8 万元**，包含接口联调、测试上线、域名绑定及三年网站维护；压缩页面定制和非核心管理能力后，可采用约 **38.8 万元**的精简方案。详细范围、预算和里程碑以 `docs/plan.md` 为准。

本期不建设多语言，不接入短信、邮件、支付，不建设复杂 BI，不进行深度智慧校园业务集成。范围外需求须单独进行变更评估。

---

# 二、推荐技术栈

## 1. 后端技术栈

建议使用：

```text
Java 25
Spring Boot 4.1.0
Spring Security OAuth2 Resource Server
MyBatis Spring Boot Starter 4.0.1 / MyBatis
MySQL 8.x
Redis（按需启用）
MinIO / 本地文件存储
Spring Security JwtEncoder / JwtDecoder（HS256）
Gradle 9.x
Springdoc OpenAPI 3.0.3 / Swagger UI
```

当前工程已完成从 JJWT 和自定义认证过滤器到 Spring Security OAuth2 Resource Server 的迁移。令牌由 Spring `JwtEncoder` 签发，`JwtDecoder` 负责验签并校验签发方，业务代码不再直接依赖 JJWT。该模式仅用于本系统账号登录后的 Bearer Token 鉴权，不建设 OAuth2 授权服务器，也不引入第三方登录。

Spring Boot 4.1 依赖、JWT 签发链路、认证 principal、密码哈希兼容和测试基线已按 `docs/plan.md` 的 P3.1 清单闭环。

## 2. 前端技术栈

建议使用：

```text
Vue 3
TypeScript
Vite
Pinia
Vue Router
Element Plus
Axios
```

## 3. 官网前台

官网前台可以使用：

```text
Vue 3 + Vite
或 Nuxt.js / SSR 方案
```

如果客户 SEO 要求较高，建议后期考虑 SSR 或静态化方案。

---

# 三、系统整体架构

```text
高校官网系统
├── 官网前台
│   ├── 首页
│   ├── 新闻中心
│   ├── 学校概况
│   ├── 机构设置
│   ├── 教育教学
│   ├── 学生工作
│   ├── 公共服务
│   ├── 搜索页
│   └── 招生就业专题站
│
├── CMS 后台管理系统
│   ├── 登录认证
│   ├── 用户管理
│   ├── 角色权限管理
│   ├── 栏目管理
│   ├── 内容管理
│   ├── Banner 管理
│   ├── 媒体库管理
│   ├── 招生就业内容管理
│   ├── SEO 配置
│   ├── 操作日志
│   └── 系统配置
│
├── 后端 API 服务
│   ├── 前台公开 API
│   ├── 后台管理 API
│   ├── 文件上传 API
│   ├── 搜索 API
│   └── 前后台联调接口
│
└── 基础设施
    ├── MySQL
    ├── Redis（按需启用）
    ├── 文件存储
    ├── Nginx
    └── 部署脚本
```

---

# 四、项目目录规划

建议采用前后端分离结构：

```text
college-official-website/
├── backend/
│   ├── src/main/java/com/example/collegeweb/
│   │   ├── CollegeWebApplication.java
│   │   ├── common/
│   │   ├── config/
│   │   ├── security/
│   │   ├── modules/
│   │   │   ├── auth/
│   │   │   ├── user/
│   │   │   ├── role/
│   │   │   ├── permission/
│   │   │   ├── column/
│   │   │   ├── content/
│   │   │   ├── banner/
│   │   │   ├── media/
│   │   │   ├── site/
│   │   │   ├── seo/
│   │   │   ├── recruit/
│   │   │   └── log/
│   │   └── infrastructure/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── mapper/
│   │   └── db/
│   └── pom.xml
│
├── admin-web/
│   ├── src/
│   │   ├── api/
│   │   ├── views/
│   │   ├── router/
│   │   ├── store/
│   │   ├── components/
│   │   └── utils/
│   └── package.json
│
├── portal-web/
│   ├── src/
│   │   ├── api/
│   │   ├── views/
│   │   ├── router/
│   │   ├── components/
│   │   └── assets/
│   └── package.json
│
├── docs/
│   ├── 需求说明.md
│   ├── 数据库设计.md
│   ├── 接口文档.md
│   ├── 部署说明.md
│   └── 测试说明.md
│
└── docker-compose.yml
```

---

# 五、核心业务模块规划

## 1. 登录认证模块

### 功能

```text
后台管理员登录
Spring JwtEncoder 生成 JWT Token
OAuth2 Resource Server 校验 Bearer Token
当前用户信息查询
退出登录
密码修改
登录失败处理
```

### 后端接口

```text
POST /api/admin/auth/login
POST /api/admin/auth/logout
GET  /api/admin/auth/me
PUT  /api/admin/auth/password
```

### 核心表

```text
sys_user
sys_role
sys_permission
sys_user_role
sys_role_permission
```

---

## 2. 用户与角色权限模块

### 功能

```text
后台账号管理
角色管理
菜单权限管理
按钮权限控制
账号启用/禁用
密码重置
```

### 权限模型

```text
超级管理员：拥有全部权限
网站管理员：管理栏目、内容、Banner、媒体库
内容编辑：发布和维护指定栏目内容
内容审核员：审核内容
招生就业管理员：维护招生就业专题站内容
```

---

## 3. 栏目管理模块

### 功能

```text
栏目新增
栏目编辑
栏目删除
栏目排序
栏目启用/禁用
栏目层级管理
栏目模板配置
是否显示在导航栏
SEO 标题、关键词、描述配置
```

### 栏目类型

```text
单页栏目
列表栏目
图片栏目
下载栏目
外链栏目
专题栏目
```

### 核心接口

```text
GET    /api/admin/columns/tree
POST   /api/admin/columns
PUT    /api/admin/columns/{id}
DELETE /api/admin/columns/{id}
PUT    /api/admin/columns/{id}/status
PUT    /api/admin/columns/sort
```

---

## 4. 内容管理模块

### 功能

```text
文章新增
文章编辑
文章删除
文章发布
文章撤回
草稿保存
置顶
推荐
排序
封面图
附件管理
浏览量统计
发布时间设置
按栏目查询内容
```

### 内容状态

```text
DRAFT       草稿
PUBLISHED   已发布
OFFLINE     已下线
DELETED     已删除
```

如果后续确认需要审核流程，可以扩展为：

```text
DRAFT        草稿
PENDING      待审核
REJECTED     已驳回
APPROVED     已审核
PUBLISHED    已发布
OFFLINE      已下线
```

---

## 5. Banner 管理模块

### 功能

```text
首页 Banner 管理
专题站 Banner 管理
Banner 图片上传
跳转链接配置
排序
启用/禁用
展示时间范围
```

### 核心接口

```text
GET    /api/admin/banners
POST   /api/admin/banners
PUT    /api/admin/banners/{id}
DELETE /api/admin/banners/{id}
PUT    /api/admin/banners/{id}/status
```

---

## 6. 媒体库管理模块

### 功能

```text
图片上传
附件上传
视频上传预留
文件分类
文件查询
文件删除
文件预览
文件引用关系预留
```

### 文件类型

```text
IMAGE
DOCUMENT
VIDEO
OTHER
```

### 存储方式

第一版建议支持两种：

```text
local：本地存储
minio：对象存储
```

通过配置切换：

```yaml
file:
  storage-type: local
  local-path: /data/college-web/uploads
  public-url-prefix: /uploads
```

---

## 7. 官网前台模块

### 页面规划

```text
首页
新闻中心
学校概况
机构设置
教育教学
学生工作
公共服务
招生就业专题站
搜索页
内容详情页
```

### 首页功能

```text
顶部导航
Logo 区
Banner 轮播
新闻动态
通知公告
学校概况入口
招生就业入口
校园服务入口
友情链接
底部版权信息
```

### 列表页功能

```text
栏目标题
面包屑导航
栏目子导航
文章列表
分页
发布时间
封面图展示
```

### 详情页功能

```text
标题
发布时间
来源
浏览量
正文内容
附件下载
上一篇/下一篇
分享预留
```

---

## 8. 招生就业专题站模块

### 第一版建议范围

```text
专题站首页
招生信息列表
就业信息列表
校企合作信息列表
政策公告列表
内容详情页
Banner 管理
专题栏目管理
```

### 本期不建设

```text
在线报名
咨询留言
留言回复
报名数据导出
企业招聘信息发布
数据统计
短信/邮件通知
```

以上互动类功能不计入本项目报价。后续确有需求时，按独立变更重新评估，不在本期预留复杂业务实现。

---

## 9. 搜索模块

### 第一版

```text
按关键词搜索文章标题
按栏目筛选
按发布时间排序
分页展示
```

### 后续增强

```text
全文检索
附件内容检索
搜索关键词高亮
热门搜索
搜索日志统计
```

---

## 10. SEO 配置模块

### 功能

```text
站点标题配置
站点关键词配置
站点描述配置
栏目 SEO 配置
文章 SEO 配置
友情链接管理
站点地图 sitemap.xml 预留
robots.txt 预留
```

---

## 11. 系统配置模块

### 功能

```text
网站名称
网站 Logo
网站备案号
联系电话
联系地址
版权信息
友情链接
首页展示数量配置
默认 SEO 配置
```

---

## 12. 日志模块

### 功能

```text
登录日志
操作日志
内容发布日志
文件上传日志
接口异常日志
```

---

# 六、数据库表设计规划

第一版建议至少包含以下表：

```text
sys_user                用户表
sys_role                角色表
sys_permission          权限表
sys_user_role           用户角色关系表
sys_role_permission     角色权限关系表

cms_column              栏目表
cms_content             内容表
cms_content_attachment  内容附件表
cms_banner              Banner 表
cms_media               媒体文件表
cms_site_config         站点配置表
cms_friend_link         友情链接表

cms_recruit_column      招生就业栏目表，可选
cms_recruit_content     招生就业内容表，可选

sys_operation_log       操作日志表
sys_login_log           登录日志表
```

如果招生就业专题站和官网内容模型一致，可以不单独建 `cms_recruit_content`，而是通过栏目类型或站点类型区分。

推荐字段：

```text
site_type:
  MAIN_SITE      主站
  RECRUIT_SITE   招生就业专题站
```

---

# 七、统一返回格式

后端所有接口统一返回：

```json
{
  "code": "000000",
  "msg": "success",
  "data": {}
}
```

分页接口返回：

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

错误示例：

```json
{
  "code": "A0400",
  "msg": "参数校验失败",
  "data": null
}
```

---

# 八、开发阶段规划

## 第一阶段：项目基础搭建

### 目标

完成后端、后台前端、官网前台基础工程搭建。

### 任务

```text
1. 创建 backend Spring Boot 项目
2. 创建 admin-web Vue 3 项目
3. 创建 portal-web Vue 3 项目
4. 配置 MySQL、可选 Redis、文件上传
5. 设计统一返回结构
6. 设计全局异常处理
7. 接入 OpenAPI 接口文档
8. 完成基础代码规范
```

### 交付物

```text
可启动的后端服务
可启动的后台管理端
可启动的官网前台
基础接口文档
```

---

## 第二阶段：后台基础能力开发

### 目标

完成 CMS 后台基础框架。

### 任务

```text
1. 登录认证
2. 用户管理
3. 角色管理
4. 权限管理
5. 菜单管理
6. 操作日志
7. 登录日志
```

### 交付物

```text
后台可登录
可创建用户
可分配角色
可控制菜单权限
```

---

## 第三阶段：CMS 核心功能开发

### 目标

完成内容管理能力。

### 任务

```text
1. 栏目管理
2. 内容管理
3. Banner 管理
4. 媒体库管理
5. 友情链接管理
6. 站点配置管理
7. SEO 配置
```

### 交付物

```text
后台可维护栏目
后台可发布新闻
后台可上传图片和附件
后台可配置 Banner
后台可配置网站基础信息
```

---

## 第四阶段：官网前台开发

### 目标

完成官网前台页面展示。

### 任务

```text
1. 首页开发
2. 新闻中心页面
3. 栏目列表页
4. 内容详情页
5. 学校概况单页
6. 公共服务页面
7. 搜索页
8. 响应式适配
```

### 交付物

```text
官网前台可访问
内容可由后台维护并动态展示
PC 和移动端基本适配
```

---

## 第五阶段：招生就业专题站开发

### 目标

完成招生就业专题站。

### 任务

```text
1. 专题站首页
2. 招生信息列表
3. 就业信息列表
4. 校企合作列表
5. 专题详情页
6. 专题 Banner
7. 后台专题内容维护
```

### 交付物

```text
招生就业专题站可独立访问
内容可由后台维护
支持独立栏目和 Banner
```

---

## 第六阶段：接口联调与扩展预留

### 目标

完成 RESTful API 规范、接口文档和预留扩展点。

### 任务

```text
1. 输出 OpenAPI 接口文档
2. 统一接口返回格式
3. 统一错误码
4. 预留统一身份认证接口
5. 预留第三方系统对接接口
6. 编写接口联调说明
```

### 交付物

```text
接口文档
联调说明
基础对接能力
```

---

## 第七阶段：测试与上线

### 目标

完成质量保障和部署上线。

### 任务

```text
1. 功能测试
2. 接口测试
3. 兼容性测试
4. 移动端适配测试
5. 权限测试
6. 文件上传测试
7. 基础安全检查
8. Nginx 部署
9. 数据库初始化脚本
10. 部署文档
```

### 交付物

```text
测试报告
部署说明
初始化 SQL
上线包
```

---

# 九、Codex 开发任务拆分

可以按下面顺序让 Codex 开发。

## 任务 1：初始化后端项目

```text
请基于 Java 25 + Spring Boot 4.1.0 创建高校官网 CMS 后端项目。

要求：
1. 使用 Gradle 9.x。
2. 包名为 com.example.collegeweb。
3. 集成 Spring MVC、Spring Validation、Spring Security OAuth2 Resource Server、MyBatis、MySQL，并按需启用 Redis。
4. 设计统一返回对象 ApiResult<T>。
5. 设计分页返回对象 PageResult<T>。
6. 设计全局异常处理 GlobalExceptionHandler。
7. 设计基础错误码枚举 ErrorCode。
8. 提供 application.yml 示例配置。
9. 提供数据库初始化目录 src/main/resources/db。
```

---

## 任务 2：设计数据库初始化 SQL

```text
请为高校官网 CMS 系统设计 MySQL 8 初始化 SQL。

需要包含：
1. 用户表 sys_user
2. 角色表 sys_role
3. 权限表 sys_permission
4. 用户角色关系表 sys_user_role
5. 角色权限关系表 sys_role_permission
6. 栏目表 cms_column
7. 内容表 cms_content
8. 内容附件表 cms_content_attachment
9. Banner 表 cms_banner
10. 媒体库表 cms_media
11. 站点配置表 cms_site_config
12. 友情链接表 cms_friend_link
13. 操作日志表 sys_operation_log
14. 登录日志表 sys_login_log

要求：
1. 所有表包含 id、created_at、updated_at、created_by、updated_by、deleted 字段。
2. 使用逻辑删除。
3. 重要查询字段建立索引。
4. 提供默认管理员账号初始化数据。
```

---

## 任务 3：开发登录认证模块

```text
请开发后台登录认证模块。

要求：
1. 支持账号密码登录。
2. 登录成功返回 JWT Token。
3. 提供获取当前用户信息接口。
4. 支持退出登录。
5. 使用 Spring Security OAuth2 Resource Server 进行 Bearer Token 鉴权。
6. 未登录返回 401。
7. 无权限返回 403。
8. 使用 Spring `JwtEncoder` 和 `JwtDecoder` 完成 HS256 签发与校验，不引入 JJWT。
9. JWT 至少包含 `iss`、`sub`、`uid`、`authorities`、`iat` 和 `exp`，角色权限按当前数据库状态装载。
10. 密码使用 BCrypt；使用委派编码器时，数据库密码必须带 `{bcrypt}` 前缀或提供旧哈希兼容方案。
11. 接口统一使用 ApiResult 返回。
```

---

## 任务 4：开发用户、角色、权限模块

```text
请开发后台用户、角色、权限管理模块。

要求：
1. 用户新增、编辑、删除、分页查询、启用禁用、重置密码。
2. 角色新增、编辑、删除、分页查询。
3. 权限菜单树查询。
4. 给用户分配角色。
5. 给角色分配权限。
6. 返回当前用户拥有的菜单和按钮权限。
7. 所有写操作记录操作日志。
```

---

## 任务 5：开发栏目管理模块

```text
请开发 CMS 栏目管理模块。

栏目字段包括：
1. 栏目名称
2. 栏目编码
3. 父级栏目 ID
4. 栏目类型：单页、列表、图片、下载、外链、专题
5. 路由路径
6. 外链地址
7. 排序号
8. 是否显示
9. 是否启用
10. SEO 标题
11. SEO 关键词
12. SEO 描述
13. 站点类型：主站、招生就业站

接口包括：
1. 栏目树查询
2. 新增栏目
3. 编辑栏目
4. 删除栏目
5. 启用禁用栏目
6. 栏目排序
```

---

## 任务 6：开发内容管理模块

```text
请开发 CMS 内容管理模块。

内容字段包括：
1. 标题
2. 副标题
3. 所属栏目 ID
4. 内容摘要
5. 正文 HTML
6. 封面图
7. 来源
8. 作者
9. 发布时间
10. 状态：草稿、已发布、已下线
11. 是否置顶
12. 是否推荐
13. 排序号
14. 浏览量
15. SEO 标题
16. SEO 关键词
17. SEO 描述

接口包括：
1. 内容分页查询
2. 内容详情查询
3. 新增内容
4. 编辑内容
5. 删除内容
6. 发布内容
7. 下线内容
8. 置顶/取消置顶
9. 推荐/取消推荐
10. 上传附件并绑定内容
```

---

## 任务 7：开发 Banner 管理模块

```text
请开发 Banner 管理模块。

Banner 字段包括：
1. 标题
2. 图片地址
3. 跳转链接
4. 展示位置：首页、招生就业站、栏目页
5. 排序号
6. 是否启用
7. 开始时间
8. 结束时间

接口包括：
1. Banner 分页查询
2. 新增 Banner
3. 编辑 Banner
4. 删除 Banner
5. 启用禁用 Banner
6. 前台查询可展示 Banner
```

---

## 任务 8：开发媒体库模块

```text
请开发媒体库模块。

要求：
1. 支持图片、文档、视频、其他文件上传。
2. 支持本地存储。
3. 预留 MinIO 存储实现。
4. 保存文件名称、原始文件名、文件大小、文件类型、访问地址、上传人、上传时间。
5. 支持文件分页查询。
6. 支持按类型查询。
7. 支持删除文件。
8. 限制文件大小和文件后缀。
```

---

## 任务 9：开发前台公开 API

```text
请开发官网前台公开 API。

接口包括：
1. 获取站点配置
2. 获取导航栏目树
3. 获取首页 Banner
4. 获取首页新闻列表
5. 获取栏目详情
6. 获取栏目内容列表
7. 获取文章详情
8. 增加文章浏览量
9. 搜索文章
10. 获取友情链接
11. 获取招生就业专题站首页数据
```

---

## 任务 10：初始化后台管理前端

```text
请基于 Vue 3 + TypeScript + Vite + Element Plus 创建后台管理前端。

要求：
1. 登录页
2. 首页布局
3. 左侧菜单
4. 顶部用户信息
5. 路由权限控制
6. Axios 请求封装
7. Token 存储与请求拦截
8. ApiResult 响应处理
9. 统一错误提示
```

---

## 任务 11：开发后台管理页面

```text
请开发 CMS 后台管理页面。

页面包括：
1. 用户管理
2. 角色管理
3. 权限管理
4. 栏目管理
5. 内容管理
6. Banner 管理
7. 媒体库管理
8. 友情链接管理
9. 站点配置
10. 操作日志
11. 登录日志
```

---

## 任务 12：初始化官网前台

```text
请基于 Vue 3 + TypeScript + Vite 创建高校官网前台项目。

页面包括：
1. 首页
2. 新闻中心
3. 栏目列表页
4. 内容详情页
5. 学校概况单页
6. 机构设置
7. 教育教学
8. 学生工作
9. 公共服务
10. 搜索页
11. 招生就业专题站首页
12. 招生就业内容列表页
13. 招生就业详情页

要求：
1. 响应式布局。
2. PC 和移动端适配。
3. 统一请求封装。
4. 支持动态栏目导航。
5. 内容从后端 API 获取。
```

---

# 十、第一版 MVP 范围

为了方便快速启动，第一版建议只做这些：

```text
1. 后台登录
2. 用户角色权限
3. 栏目管理
4. 内容管理
5. Banner 管理
6. 媒体库
7. 站点配置
8. 官网首页
9. 列表页
10. 详情页
11. 招生就业专题站展示
12. 搜索
13. 部署脚本
```

暂不做：

```text
1. 在线报名
2. 留言咨询
3. 多级审核流
4. 统一身份认证
5. 旧站数据迁移
6. 多语言
7. 等保整改
8. 复杂全文检索
9. 视频转码
```

同时不建设复杂站群、短信、邮件、支付、复杂 BI 和深度智慧校园业务集成。以上内容不属于当前报价，新增时应单独评估。

---

# 十一、验收标准

## 1. 后台验收

```text
管理员可以登录后台
可以维护用户、角色、权限
可以维护栏目
可以发布新闻内容
可以上传图片和附件
可以维护 Banner
可以配置网站基础信息
可以查看操作日志
```

## 2. 前台验收

```text
官网首页正常展示
导航栏目动态展示
新闻列表正常分页
新闻详情正常展示
Banner 正常轮播
招生就业专题站正常展示
搜索功能可用
PC 和移动端展示正常
```

## 3. 接口验收

```text
接口返回格式统一
异常信息统一
接口文档完整
接口参数校验有效
未登录不能访问后台接口
无权限不能访问受限接口
```

## 4. 部署验收

```text
后端服务可启动
前端可通过 Nginx 访问
数据库初始化脚本可执行
文件上传可访问
部署文档完整
```

---

# 十二、建议 Codex 总体开发指令

你可以直接把下面这段作为总 Prompt 放进 Codex：

```text
请按照以下规划，帮我设计并开发一个“高校官网建设项目”。

项目采用前后端分离架构：

后端：
- Java 25
- Spring Boot 4.1.0
- Spring Security OAuth2 Resource Server
- MyBatis Spring Boot Starter 4.0.1
- MySQL 8
- Redis（按需启用）
- Spring Security JwtEncoder / JwtDecoder（HS256）
- Springdoc OpenAPI 3.0.3 / Swagger UI
- 统一返回格式 ApiResult<T>
- 全局异常处理
- 参数校验
- 操作日志

后台管理端：
- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios

官网前台：
- Vue 3
- TypeScript
- Vite
- 响应式布局

系统包括：
1. 官网前台展示
2. 招生就业专题站
3. CMS 后台管理
4. 用户角色权限
5. 栏目管理
6. 内容管理
7. Banner 管理
8. 媒体库管理
9. 站点配置
10. 友情链接
11. SEO 配置
12. 搜索
13. 操作日志
14. 登录日志

请先完成以下内容：
1. 生成完整项目目录结构。
2. 生成后端 Spring Boot 基础工程。
3. 设计 MySQL 初始化 SQL。
4. 实现统一返回对象 ApiResult。
5. 实现分页返回对象 PageResult。
6. 实现全局异常处理。
7. 实现错误码枚举。
8. 实现登录认证模块。
9. 实现用户、角色、权限模块。
10. 实现栏目管理模块。
11. 实现内容管理模块。
12. 实现 Banner 管理模块。
13. 实现媒体库模块。
14. 实现前台公开 API。
15. 生成后台管理端 Vue 项目基础代码。
16. 生成官网前台 Vue 项目基础代码。
17. 提供 README、部署说明和接口说明。

开发要求：
1. 代码结构清晰，按模块划分。
2. Controller、Service、Mapper、DTO、VO、Entity 分层明确。
3. 所有接口使用统一返回格式。
4. 所有入参使用 Validation 校验。
5. 所有数据库表使用逻辑删除。
6. 所有写操作记录操作日志。
7. 接口命名符合 RESTful 风格。
8. 代码中添加必要注释。
9. 优先完成轻量化交付，不实现复杂站群、在线报名、留言咨询、多级审核流、统一身份认证、多语言、短信、邮件、支付、复杂 BI、深度智慧校园集成和等保整改。
```

---

这版规划适合按轻量化范围推进。若后续提出在线报名、留言咨询、统一认证、审核流或旧站迁移，应先走需求变更并重新评估费用、工期和架构影响。
