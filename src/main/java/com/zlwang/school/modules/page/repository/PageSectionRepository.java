package com.zlwang.school.modules.page.repository;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;

public interface PageSectionRepository {

    List<PageSection> findAll(SiteType siteType, PageCode pageCode);

    void replace(SiteType siteType, PageCode pageCode, List<SavePageSection> sections, long operatorId);
}
