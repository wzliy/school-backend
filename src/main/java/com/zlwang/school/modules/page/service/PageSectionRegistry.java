package com.zlwang.school.modules.page.service;

import static com.zlwang.school.modules.page.model.PageCode.HOME;
import static com.zlwang.school.modules.page.model.PageCode.RECRUIT_HOME;
import static com.zlwang.school.modules.page.model.PageSectionType.CONTACT_INFO;
import static com.zlwang.school.modules.page.model.PageSectionType.CONTENT_FEED;
import static com.zlwang.school.modules.page.model.PageSectionType.FRIEND_LINKS;
import static com.zlwang.school.modules.page.model.PageSectionType.HERO_BANNER;
import static com.zlwang.school.modules.page.model.PageSectionType.IMAGE_GALLERY;
import static com.zlwang.school.modules.page.model.PageSectionType.QUICK_LINKS;
import static com.zlwang.school.modules.template.model.SiteType.MAIN_SITE;
import static com.zlwang.school.modules.template.model.SiteType.RECRUIT_SITE;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageDefinition;
import com.zlwang.school.modules.page.model.PageSectionDefinition;
import com.zlwang.school.modules.page.model.PageSectionType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PageSectionRegistry {

    private final Map<PageCode, PageDefinition> pages;

    public PageSectionRegistry() {
        Map<PageCode, PageDefinition> registered = new LinkedHashMap<>();
        registered.put(HOME, new PageDefinition(HOME, MAIN_SITE, List.of(
            fixed("HERO", HERO_BANNER, styles("FULL_WIDTH")),
            content("SCHOOL_NEWS"),
            content("NOTICE"),
            optionalCount("QUICK_LINKS", QUICK_LINKS, styles("ICON_GRID", "TEXT_LIST"), 1, 30),
            optionalCount("CAMPUS_GALLERY", IMAGE_GALLERY, styles("GRID", "CAROUSEL"), 1, 20),
            optionalCount("FRIEND_LINKS", FRIEND_LINKS, styles("TEXT_LINKS", "LOGO_GRID"), 1, 50)
        )));
        registered.put(RECRUIT_HOME, new PageDefinition(RECRUIT_HOME, RECRUIT_SITE, List.of(
            fixed("HERO", HERO_BANNER, styles("FULL_WIDTH")),
            content("ADMISSION_NEWS"),
            content("EMPLOYMENT_NEWS"),
            content("SCHOOL_ENTERPRISE"),
            content("RECRUIT_POLICY"),
            optionalCount("QUICK_LINKS", QUICK_LINKS, styles("ICON_GRID", "TEXT_LIST"), 1, 30),
            fixed("CONTACT", CONTACT_INFO, styles("DEFAULT", "COMPACT"))
        )));
        pages = Map.copyOf(registered);
    }

    public PageDefinition get(PageCode pageCode) {
        return pages.get(pageCode);
    }

    private static PageSectionDefinition content(String code) {
        return new PageSectionDefinition(
            code,
            CONTENT_FEED,
            styles("TEXT_LIST", "IMAGE_TEXT", "CARD"),
            true,
            true,
            true,
            true,
            1,
            20
        );
    }

    private static PageSectionDefinition fixed(
        String code,
        PageSectionType type,
        Set<String> styles
    ) {
        return new PageSectionDefinition(code, type, styles, false, false, false, false, 0, 0);
    }

    private static PageSectionDefinition optionalCount(
        String code,
        PageSectionType type,
        Set<String> styles,
        int min,
        int max
    ) {
        return new PageSectionDefinition(code, type, styles, false, false, true, false, min, max);
    }

    private static Set<String> styles(String... values) {
        return Set.of(values);
    }
}
