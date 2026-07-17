package com.zlwang.school.modules.site.repository;

import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import java.util.List;

public interface CmsSiteConfigRepository {

    List<CmsSiteConfig> findAll(SiteScope siteType);

    boolean updateValues(SiteScope siteType, List<UpdateSiteConfigValue> values, long operatorId);
}
