package com.zlwang.school.modules.template.service;

import static com.zlwang.school.modules.template.model.ColumnType.DOWNLOAD;
import static com.zlwang.school.modules.template.model.ColumnType.IMAGE;
import static com.zlwang.school.modules.template.model.ColumnType.LIST;
import static com.zlwang.school.modules.template.model.ColumnType.PAGE;
import static com.zlwang.school.modules.template.model.ColumnType.SPECIAL;
import static com.zlwang.school.modules.template.model.EditorFieldType.COLUMN_SELECT;
import static com.zlwang.school.modules.template.model.EditorFieldType.CONTENT_SELECT;
import static com.zlwang.school.modules.template.model.EditorFieldType.DATETIME;
import static com.zlwang.school.modules.template.model.EditorFieldType.FILE_LIST;
import static com.zlwang.school.modules.template.model.EditorFieldType.IMAGE_LIST;
import static com.zlwang.school.modules.template.model.EditorFieldType.LINK;
import static com.zlwang.school.modules.template.model.EditorFieldType.NUMBER;
import static com.zlwang.school.modules.template.model.EditorFieldType.RICH_TEXT;
import static com.zlwang.school.modules.template.model.EditorFieldType.SELECT;
import static com.zlwang.school.modules.template.model.EditorFieldType.SWITCH;
import static com.zlwang.school.modules.template.model.EditorFieldType.TEXT;
import static com.zlwang.school.modules.template.model.EditorFieldType.TEXTAREA;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_DETAIL;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_LIST;
import static com.zlwang.school.modules.template.model.PageTemplateKey.HOME;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ORGANIZATION;
import static com.zlwang.school.modules.template.model.PageTemplateKey.RECRUIT_HOME;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SEARCH_RESULT;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SERVICE_DIRECTORY;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SINGLE_PAGE;
import static com.zlwang.school.modules.template.model.SiteType.MAIN_SITE;
import static com.zlwang.school.modules.template.model.SiteType.RECRUIT_SITE;
import static com.zlwang.school.modules.template.model.TemplateUsage.COLUMN;
import static com.zlwang.school.modules.template.model.TemplateUsage.DETAIL;
import static com.zlwang.school.modules.template.model.TemplateUsage.LANDING;
import static com.zlwang.school.modules.template.model.TemplateUsage.SYSTEM;

import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.EditorFieldOption;
import com.zlwang.school.modules.template.model.EditorFieldType;
import com.zlwang.school.modules.template.model.EditorSchema;
import com.zlwang.school.modules.template.model.FieldValidationRule;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PageTemplateRegistry {

    private static final List<SiteType> ALL_SITES = List.of(MAIN_SITE, RECRUIT_SITE);
    private static final List<ColumnType> ARTICLE_COLUMN_TYPES = List.of(LIST, IMAGE, DOWNLOAD);

    private final Map<PageTemplateKey, PageTemplateDefinition> definitions;

    public PageTemplateRegistry() {
        Map<PageTemplateKey, PageTemplateDefinition> registered = new LinkedHashMap<>();
        register(registered, home());
        register(registered, articleList());
        register(registered, articleDetail());
        register(registered, singlePage());
        register(registered, organization());
        register(registered, serviceDirectory());
        register(registered, recruitHome());
        register(registered, searchResult());
        definitions = Map.copyOf(registered);
    }

    public List<PageTemplateDefinition> findAll() {
        return List.of(
            definitions.get(HOME),
            definitions.get(ARTICLE_LIST),
            definitions.get(ARTICLE_DETAIL),
            definitions.get(SINGLE_PAGE),
            definitions.get(ORGANIZATION),
            definitions.get(SERVICE_DIRECTORY),
            definitions.get(RECRUIT_HOME),
            definitions.get(SEARCH_RESULT)
        );
    }

    public Optional<PageTemplateDefinition> findByKey(PageTemplateKey templateKey) {
        return Optional.ofNullable(definitions.get(templateKey));
    }

    public Optional<EditorSchema> findEditorSchema(PageTemplateKey templateKey) {
        return findByKey(templateKey).map(PageTemplateDefinition::editorSchema);
    }

    public boolean isCompatible(PageTemplateKey templateKey, SiteType siteType, ColumnType columnType) {
        return findByKey(templateKey)
            .map(template -> template.supports(siteType, columnType))
            .orElse(false);
    }

    private static void register(
        Map<PageTemplateKey, PageTemplateDefinition> registered,
        PageTemplateDefinition definition
    ) {
        if (registered.put(definition.templateKey(), definition) != null) {
            throw new IllegalStateException("页面模板编码重复: " + definition.templateKey());
        }
    }

    private static PageTemplateDefinition home() {
        return template(
            HOME,
            "官网首页",
            "主站首页，内容由站点配置、Banner 和预定义页面区块聚合。",
            LANDING,
            List.of(MAIN_SITE),
            List.of(SPECIAL),
            null,
            schema(List.of(), List.of(), List.of(), List.of(
                field("pageTitle", "首页标题", TEXT, true, null, "请输入浏览器标题", text(255), 10),
                field("backgroundImage", "首页背景图", EditorFieldType.IMAGE, false, null, null, text(512), 20),
                field("enabled", "首页状态", SWITCH, true, true, null, none(), 30),
                field("seoTitle", "SEO 标题", TEXT, false, null, "留空时使用站点默认标题", text(255), 40),
                field("seoKeywords", "SEO 关键词", TEXT, false, null, "多个关键词用逗号分隔", text(512), 50),
                field("seoDescription", "SEO 描述", TEXTAREA, false, null, null, text(1024), 60)
            ))
        );
    }

    private static PageTemplateDefinition articleList() {
        return template(
            ARTICLE_LIST,
            "文章列表页",
            "用于新闻、通知、招生信息等内容分页列表。",
            COLUMN,
            ALL_SITES,
            ARTICLE_COLUMN_TYPES,
            ARTICLE_DETAIL,
            schema(List.of(
                field("coverUrl", "栏目 Banner", EditorFieldType.IMAGE, false, null, null, text(512), 10),
                select("listStyle", "列表样式", true, "IMAGE_TEXT", 20,
                    option("TEXT", "纯文字"), option("IMAGE_TEXT", "图文"), option("CARD", "卡片")),
                field("pageSize", "每页数量", NUMBER, true, 10, "请输入 5-50 之间的整数", number(5, 50), 30),
                field("showCover", "显示封面图", SWITCH, true, true, null, none(), 40),
                field("showSummary", "显示摘要", SWITCH, true, true, null, none(), 50),
                field("showPublishAt", "显示发布时间", SWITCH, true, true, null, none(), 60),
                field("showViewCount", "显示浏览量", SWITCH, true, false, null, none(), 70),
                select("defaultSort", "默认排序", true, "PUBLISH_AT_DESC", 80,
                    option("PUBLISH_AT_DESC", "发布时间倒序"), option("SORT_NO_ASC", "排序号升序")),
                field("emptyText", "空数据提示", TEXT, true, "暂无相关内容", null, text(128), 90)
            ), articleFields(true), List.of(), List.of())
        );
    }

    private static PageTemplateDefinition articleDetail() {
        return template(
            ARTICLE_DETAIL,
            "文章详情页",
            "文章正文、附件和上下篇等详情展示。",
            DETAIL,
            ALL_SITES,
            ARTICLE_COLUMN_TYPES,
            null,
            schema(List.of(
                field("showAuthor", "显示作者", SWITCH, true, true, null, none(), 10),
                field("showSource", "显示来源", SWITCH, true, true, null, none(), 20),
                field("showPublishAt", "显示发布时间", SWITCH, true, true, null, none(), 30),
                field("showViewCount", "显示浏览量", SWITCH, true, true, null, none(), 40),
                field("showSiblingNavigation", "显示上一篇/下一篇", SWITCH, true, true, null, none(), 50),
                field("showAttachments", "显示附件区域", SWITCH, true, true, null, none(), 60),
                field("showRelated", "显示相关推荐", SWITCH, true, false, null, none(), 70),
                field("relatedCount", "相关推荐数量", NUMBER, false, 4, "请输入 1-10 之间的整数", number(1, 10), 80),
                field("shareEnabled", "允许分享", SWITCH, true, false, null, none(), 90)
            ), articleFields(true), List.of(), List.of())
        );
    }

    private static PageTemplateDefinition singlePage() {
        return template(
            SINGLE_PAGE,
            "单页图文页",
            "用于学校简介、办学理念、历史沿革和联系我们等单页内容。",
            COLUMN,
            ALL_SITES,
            List.of(PAGE),
            null,
            schema(List.of(
                field("coverUrl", "页面 Banner", EditorFieldType.IMAGE, false, null, null, text(512), 10)
            ), articleFields(true), List.of(
                field("primaryImage", "主图片", EditorFieldType.IMAGE, false, null, null, text(512), 10),
                field("gallery", "图片集", IMAGE_LIST, false, null, null, none(), 20),
                field("contactPhone", "联系电话", TEXT, false, null, "请输入公开联系电话", text(64), 30),
                field("address", "联系地址", TEXT, false, null, null, text(255), 40),
                field("mapUrl", "地图地址", LINK, false, null, "请输入 HTTPS 地址", text(512), 50)
            ), List.of())
        );
    }

    private static PageTemplateDefinition organization() {
        return template(
            ORGANIZATION,
            "机构展示页",
            "按子栏目分类展示院系、行政部门和教学单位。",
            COLUMN,
            List.of(MAIN_SITE),
            List.of(LIST),
            null,
            schema(List.of(
                select("displayStyle", "展示样式", true, "GROUPED", 10,
                    option("GROUPED", "分类分组"), option("SIMPLE_LIST", "简单列表"))
            ), directoryContentFields(), List.of(
                field("shortName", "机构简称", TEXT, false, null, null, text(128), 10),
                field("logoUrl", "机构 Logo", EditorFieldType.IMAGE, false, null, null, text(512), 20),
                field("leader", "负责人", TEXT, false, null, null, text(64), 30),
                field("contactPhone", "联系电话", TEXT, false, null, null, text(64), 40),
                field("officeAddress", "办公地址", TEXT, false, null, null, text(255), 50),
                field("websiteUrl", "机构网址", LINK, false, null, "请输入 HTTPS 地址", text(512), 60),
                select("linkMode", "跳转方式", true, "EXTERNAL", 70,
                    option("INTERNAL", "内部页面"), option("EXTERNAL", "外部链接"))
            ), List.of())
        );
    }

    private static PageTemplateDefinition serviceDirectory() {
        return template(
            SERVICE_DIRECTORY,
            "公共服务页",
            "按子栏目分类展示 OA、图书馆和校园服务等入口。",
            COLUMN,
            List.of(MAIN_SITE),
            List.of(LIST),
            null,
            schema(List.of(
                select("displayStyle", "展示样式", true, "ICON_GRID", 10,
                    option("ICON_GRID", "图标网格"), option("SIMPLE_LIST", "简单列表"))
            ), directoryContentFields(), List.of(
                field("iconUrl", "服务图标", EditorFieldType.IMAGE, false, null, null, text(512), 10),
                field("targetUrl", "跳转地址", LINK, true, null, "请输入 HTTPS 地址", text(512), 20),
                select("serviceType", "服务类型", true, "OTHER", 30,
                    option("OA", "OA"), option("CAMPUS_CARD", "校园卡"),
                    option("ACADEMIC", "教务"), option("LIBRARY", "图书馆"), option("OTHER", "其他")),
                field("loginRequired", "需要登录", SWITCH, true, false, null, none(), 40),
                select("openMode", "打开方式", true, "NEW_WINDOW", 50,
                    option("CURRENT", "当前页"), option("NEW_WINDOW", "新窗口"))
            ), List.of())
        );
    }

    private static PageTemplateDefinition recruitHome() {
        return template(
            RECRUIT_HOME,
            "招生就业专题首页",
            "招生就业专题站入口，使用独立主视觉和预定义页面区块。",
            LANDING,
            List.of(RECRUIT_SITE),
            List.of(SPECIAL),
            null,
            schema(List.of(), List.of(), List.of(), List.of(
                field("topicName", "专题名称", TEXT, true, null, "请输入专题名称", text(128), 10),
                field("logoUrl", "专题 Logo", EditorFieldType.IMAGE, false, null, null, text(512), 20),
                field("heroImageUrl", "专题主视觉图", EditorFieldType.IMAGE, true, null, null, text(512), 30),
                field("mobileHeroImageUrl", "手机端主视觉图", EditorFieldType.IMAGE, false, null, null, text(512), 40),
                field("introduction", "专题简介", RICH_TEXT, false, null, null, none(), 50),
                field("contactPhone", "联系电话", TEXT, false, null, null, text(64), 60),
                field("consultingHours", "咨询时间", TEXT, false, null, null, text(128), 70),
                field("address", "联系地址", TEXT, false, null, null, text(255), 80),
                field("brochureContentId", "招生简章", CONTENT_SELECT, false, null, null, none(), 90),
                field("registrationUrl", "报名入口", LINK, false, null, "仅支持外部链接", text(512), 100),
                field("consultationUrl", "留言入口", LINK, false, null, "仅支持外部链接", text(512), 110),
                field("enabled", "专题状态", SWITCH, true, true, null, none(), 120),
                field("seoTitle", "SEO 标题", TEXT, false, null, null, text(255), 130),
                field("seoKeywords", "SEO 关键词", TEXT, false, null, null, text(512), 140),
                field("seoDescription", "SEO 描述", TEXTAREA, false, null, null, text(1024), 150)
            ))
        );
    }

    private static PageTemplateDefinition searchResult() {
        return template(
            SEARCH_RESULT,
            "搜索结果页",
            "主站和专题站共用的文章搜索结果页面。",
            SYSTEM,
            ALL_SITES,
            List.of(),
            null,
            schema(List.of(), List.of(), List.of(), List.of(
                select("searchScope", "搜索范围", true, "ALL", 10,
                    option("ALL", "所有文章"), option("NEWS", "新闻"),
                    option("NOTICE", "通知"), option("RECRUIT", "招生就业")),
                field("searchContent", "搜索正文", SWITCH, true, false, null, none(), 20),
                field("pageSize", "每页数量", NUMBER, true, 10, "请输入 5-50 之间的整数", number(5, 50), 30),
                field("showSummary", "显示摘要", SWITCH, true, true, null, none(), 40),
                field("showCover", "显示封面图", SWITCH, true, false, null, none(), 50),
                field("highlightKeyword", "关键词高亮", SWITCH, true, true, null, none(), 60),
                field("emptyText", "无结果提示", TEXT, true, "未找到相关内容", null, text(128), 70),
                field("hotKeywords", "热门关键词", EditorFieldType.CHECKBOX, false, null, null, none(), 80)
            ))
        );
    }

    private static List<EditorFieldDefinition> articleFields(boolean contentRequired) {
        return List.of(
            field("title", "内容标题", TEXT, true, null, "请输入内容标题", text(255), 10),
            field("subtitle", "副标题", TEXT, false, null, null, text(255), 20),
            readOnlyField("columnId", "所属栏目", COLUMN_SELECT, true, 30, "由当前栏目确定"),
            field("summary", "内容摘要", TEXTAREA, false, null, null, text(1024), 40),
            field("coverUrl", "封面图片", EditorFieldType.IMAGE, false, null, null, text(512), 50),
            field("author", "作者", TEXT, false, null, null, text(64), 60),
            field("source", "来源", TEXT, false, null, null, text(128), 70),
            field("contentHtml", "正文内容", RICH_TEXT, contentRequired, null, null, none(), 80),
            field("publishAt", "发布时间", DATETIME, true, null, null, none(), 90),
            field("attachments", "附件", FILE_LIST, false, null, null, none(), 100),
            field("topFlag", "置顶", SWITCH, true, false, null, none(), 110),
            field("recommendFlag", "推荐", SWITCH, true, false, null, none(), 120),
            field("sortNo", "排序号", NUMBER, true, 0, null, number(0, 999999), 130),
            select("status", "发布状态", true, "DRAFT", 140,
                option("DRAFT", "草稿"), option("PUBLISHED", "已发布"), option("OFFLINE", "已下线")),
            field("seoTitle", "SEO 标题", TEXT, false, null, null, text(255), 150),
            field("seoKeywords", "SEO 关键词", TEXT, false, null, null, text(512), 160),
            field("seoDescription", "SEO 描述", TEXTAREA, false, null, null, text(1024), 170)
        );
    }

    private static List<EditorFieldDefinition> directoryContentFields() {
        return List.of(
            field("title", "名称", TEXT, true, null, "请输入名称", text(255), 10),
            readOnlyField("columnId", "所属分类", COLUMN_SELECT, true, 20, "由当前子栏目确定"),
            field("summary", "简介", TEXTAREA, false, null, null, text(1024), 30),
            field("contentHtml", "详细介绍", RICH_TEXT, false, null, null, none(), 40),
            field("publishAt", "发布时间", DATETIME, true, null, null, none(), 50),
            field("recommendFlag", "推荐显示", SWITCH, true, false, null, none(), 60),
            field("sortNo", "排序号", NUMBER, true, 0, null, number(0, 999999), 70),
            select("status", "发布状态", true, "DRAFT", 80,
                option("DRAFT", "草稿"), option("PUBLISHED", "已发布"), option("OFFLINE", "已下线"))
        );
    }

    private static PageTemplateDefinition template(
        PageTemplateKey key,
        String name,
        String description,
        com.zlwang.school.modules.template.model.TemplateUsage usage,
        List<SiteType> sites,
        List<ColumnType> columnTypes,
        PageTemplateKey defaultDetailTemplateKey,
        EditorSchema editorSchema
    ) {
        return new PageTemplateDefinition(
            key,
            name,
            description,
            usage,
            sites,
            columnTypes,
            defaultDetailTemplateKey,
            editorSchema
        );
    }

    private static EditorSchema schema(
        List<EditorFieldDefinition> columnFields,
        List<EditorFieldDefinition> contentFields,
        List<EditorFieldDefinition> extensionFields,
        List<EditorFieldDefinition> pageFields
    ) {
        return EditorSchema.of(columnFields, contentFields, extensionFields, pageFields);
    }

    private static EditorFieldDefinition field(
        String code,
        String name,
        EditorFieldType type,
        boolean required,
        Object defaultValue,
        String placeholder,
        FieldValidationRule validation,
        int sort
    ) {
        return new EditorFieldDefinition(
            code,
            name,
            type,
            required,
            defaultValue,
            placeholder,
            validation,
            List.of(),
            sort,
            true,
            false,
            null
        );
    }

    private static EditorFieldDefinition readOnlyField(
        String code,
        String name,
        EditorFieldType type,
        boolean required,
        int sort,
        String helpText
    ) {
        return new EditorFieldDefinition(
            code,
            name,
            type,
            required,
            null,
            null,
            none(),
            List.of(),
            sort,
            true,
            true,
            helpText
        );
    }

    private static EditorFieldDefinition select(
        String code,
        String name,
        boolean required,
        String defaultValue,
        int sort,
        EditorFieldOption... options
    ) {
        List<EditorFieldOption> optionList = List.of(options);
        return new EditorFieldDefinition(
            code,
            name,
            SELECT,
            required,
            defaultValue,
            null,
            FieldValidationRule.choices(optionList.stream().map(EditorFieldOption::value).toArray(String[]::new)),
            optionList,
            sort,
            true,
            false,
            null
        );
    }

    private static EditorFieldOption option(String value, String label) {
        return new EditorFieldOption(value, label);
    }

    private static FieldValidationRule text(int maxLength) {
        return FieldValidationRule.text(maxLength);
    }

    private static FieldValidationRule number(long min, long max) {
        return FieldValidationRule.number(min, max);
    }

    private static FieldValidationRule none() {
        return new FieldValidationRule(null, null, null, null, null, List.of());
    }
}
