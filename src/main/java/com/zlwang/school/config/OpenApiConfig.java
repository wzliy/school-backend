package com.zlwang.school.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("高校官网 CMS API")
                .description("高校官网、招生就业专题站与 CMS 后台接口文档")
                .version("1.0.0"));
    }
}
