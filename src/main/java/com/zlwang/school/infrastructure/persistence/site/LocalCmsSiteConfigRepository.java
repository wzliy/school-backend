package com.zlwang.school.infrastructure.persistence.site;

import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.site.repository.UpdateSiteConfigValue;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsSiteConfigRepository implements CmsSiteConfigRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsSiteConfigRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public List<CmsSiteConfig> findAll(SiteScope siteType) {
        return localCmsStore.findSiteConfigs(siteType);
    }

    @Override
    public boolean updateValues(SiteScope siteType, List<UpdateSiteConfigValue> values, long operatorId) {
        return localCmsStore.updateSiteConfigs(siteType, values);
    }
}
