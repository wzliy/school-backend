package com.zlwang.school.infrastructure.persistence.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteConfigType;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.repository.UpdateSiteConfigValue;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisCmsSiteConfigRepositoryTests {

    @Mock
    private CmsSiteConfigMapper cmsSiteConfigMapper;

    private MybatisCmsSiteConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsSiteConfigRepository(cmsSiteConfigMapper);
    }

    @Test
    void mapsScopesAndConfigTypes() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsSiteConfigMapper.findAll("MAIN_SITE")).thenReturn(List.of(
            new CmsSiteConfigRow(
                13L,
                "MAIN_SITE",
                "homeNewsLimit",
                "8",
                "NUMBER",
                "首页新闻展示数量",
                now,
                now
            )
        ));

        List<CmsSiteConfig> configs = repository.findAll(SiteScope.MAIN_SITE);

        assertThat(configs).singleElement().satisfies(config -> {
            assertThat(config.siteType()).isEqualTo(SiteScope.MAIN_SITE);
            assertThat(config.configType()).isEqualTo(SiteConfigType.NUMBER);
            assertThat(config.configValue()).isEqualTo("8");
        });
    }

    @Test
    void updatesEveryValueWithScopeAndOperator() {
        when(cmsSiteConfigMapper.updateValue("GLOBAL", "siteName", "新校名", 9L)).thenReturn(1);
        when(cmsSiteConfigMapper.updateValue("GLOBAL", "contactPhone", "010-12345678", 9L))
            .thenReturn(1);

        boolean updated = repository.updateValues(
            SiteScope.GLOBAL,
            List.of(
                new UpdateSiteConfigValue("siteName", "新校名"),
                new UpdateSiteConfigValue("contactPhone", "010-12345678")
            ),
            9L
        );

        assertThat(updated).isTrue();
        verify(cmsSiteConfigMapper).updateValue("GLOBAL", "siteName", "新校名", 9L);
        verify(cmsSiteConfigMapper).updateValue("GLOBAL", "contactPhone", "010-12345678", 9L);
    }

    @Test
    void reportsConcurrentMissingConfig() {
        when(cmsSiteConfigMapper.updateValue("GLOBAL", "siteName", "新校名", 9L)).thenReturn(0);

        assertThat(repository.updateValues(
            SiteScope.GLOBAL,
            List.of(new UpdateSiteConfigValue("siteName", "新校名")),
            9L
        )).isFalse();
    }
}
