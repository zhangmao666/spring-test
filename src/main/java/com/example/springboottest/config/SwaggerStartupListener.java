package com.example.springboottest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Swagger启动监听器
 * 在应用启动完成后打印Swagger UI的访问地址
 */
@Slf4j
@Component
public class SwaggerStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();

            log.info("\n" +
                    "================================================================================\n" +
                    "  📚 Swagger API 文档已启动成功！\n" +
                    "================================================================================\n" +
                    "  Knife4j文档: http://localhost:{}{}/doc.html\n" +
                    "  Swagger UI:  http://localhost:{}{}/swagger-ui/index.html\n" +
                    "  网络访问:    http://{}:{}{}/doc.html\n" +
                    "  OpenAPI JSON: http://localhost:{}{}/v3/api-docs\n" +
                    "================================================================================\n",
                    port, contextPath, port, contextPath, localIp, port, contextPath, port, contextPath);
        } catch (UnknownHostException e) {
            log.info("\n" +
                    "================================================================================\n" +
                    "  📚 Swagger API 文档已启动成功！\n" +
                    "================================================================================\n" +
                    "  Knife4j文档: http://localhost:{}{}/doc.html\n" +
                    "  Swagger UI:  http://localhost:{}{}/swagger-ui/index.html\n" +
                    "  OpenAPI JSON: http://localhost:{}{}/v3/api-docs\n" +
                    "================================================================================\n",
                    port, contextPath, port, contextPath, port, contextPath);
        }
    }
}
