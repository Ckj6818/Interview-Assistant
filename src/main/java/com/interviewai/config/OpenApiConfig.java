package com.interviewai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI interviewOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Interview-Assistant API")
                        .description("AI 面试助手 REST 接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("Interview-Assistant")));
    }
}
