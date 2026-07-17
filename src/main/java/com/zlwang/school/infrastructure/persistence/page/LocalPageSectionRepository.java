package com.zlwang.school.infrastructure.persistence.page;

import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.repository.PageSectionRepository;
import com.zlwang.school.modules.page.repository.SavePageSection;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalPageSectionRepository implements PageSectionRepository {

    private final LocalCmsStore localCmsStore;

    public LocalPageSectionRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public List<PageSection> findAll(SiteType siteType, PageCode pageCode) {
        return localCmsStore.findPageSections(siteType, pageCode);
    }

    @Override
    public void replace(
        SiteType siteType,
        PageCode pageCode,
        List<SavePageSection> sections,
        long operatorId
    ) {
        localCmsStore.replacePageSections(sections);
    }
}
