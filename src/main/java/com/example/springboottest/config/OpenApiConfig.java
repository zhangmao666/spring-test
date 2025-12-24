package com.example.springboottest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置类
 */
@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Spring Boot 综合项目 API 文档")
                                                .version("1.0.0")
                                                .description("基于 Spring Boot 3 和 Java 17 的综合性基础项目 API 接口文档\n\n" +
                                                                "包含以下功能模块：\n" +
                                                                "- 用户认证与授权\n" +
                                                                "- 课程管理\n" +
                                                                "- 文件上传下载\n" +
                                                                "- AI 聊天对话\n" +
                                                                "- 天气查询\n" +
                                                                "- 字典管理\n" +
                                                                "- 系统监控等")
                                                .contact(new Contact()
                                                                .name("开发团队")
                                                                .email("dev@example.com")
                                                                .url("https://example.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                // 配置 JWT Bearer Token 认证
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                                                new SecurityScheme()
                                                                                .name(SECURITY_SCHEME_NAME)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("请输入 JWT Token（不需要 Bearer 前缀）")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("项目文档与源码")
                                                .url("https://github.com/example/spring-boot-test"));
        }
}
