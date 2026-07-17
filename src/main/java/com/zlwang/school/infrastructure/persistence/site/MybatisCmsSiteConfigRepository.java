package com.zlwang.school.infrastructure.persistence.site;

import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteConfigType;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.CmsSiteConfigRepository;
import com.zlwang.school.modules.site.repository.UpdateSiteConfigValue;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisCmsSiteConfigRepository implements CmsSiteConfigRepository {

    private final CmsSiteConfigMapper cmsSiteConfigMapper;

    public MybatisCmsSiteConfigRepository(CmsSiteConfigMapper cmsSiteConfigMapper) {
        this.cmsSiteConfigMapper = cmsSiteConfigMapper;
    }

    @Override
    public List<CmsSiteConfig> findAll(SiteScope siteType) {
        String scope = siteType == null ? null : siteType.name();
        return cmsSiteConfigMapper.findAll(scope).stream().map(this::toConfig).toList();
    }

    @Override
    @Transactional
    public boolean updateValues(SiteScope siteType, List<UpdateSiteConfigValue> values, long operatorId) {
        for (UpdateSiteConfigValue value : values) {
            if (cmsSiteConfigMapper.updateValue(
                siteType.name(),
                value.configKey(),
                value.configValue(),
                operatorId
            ) == 0) {
                return false;
            }
        }
        return true;
    }

    private CmsSiteConfig toConfig(CmsSiteConfigRow row) {
        return new CmsSiteConfig(
            row.id(),
            SiteScope.valueOf(row.siteType()),
            row.configKey(),
            row.configValue(),
            SiteConfigType.valueOf(row.configType()),
            row.description(),
            row.createdAt(),
            row.updatedAt()
        );
    }
}
