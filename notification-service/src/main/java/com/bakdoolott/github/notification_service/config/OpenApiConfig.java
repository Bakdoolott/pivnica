package com.bakdoolott.github.notification_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String USER_ID_HEADER_SCHEME = "X-User-Id";

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description(
                                """
                                Регистрация FCM-токенов устройств и отправка push-уведомлений пользователям. \n
                                /tokens/** вызываются мобильным клиентом через gateway (нужен X-User-Id, \n
                                который подставляет сам gateway после проверки JWT). \n
                                /notifications/send вызывается другими бэкенд-сервисами напрямую, в обход gateway.
                                """)
                        .version("v1"))
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes(USER_ID_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(USER_ID_HEADER_SCHEME)
                                .description(
                                        """
                                        Подставляется gateway из проверенного JWT. \n
                                        При прямом обращении к сервису (например, из Swagger UI в обход gateway) \n
                                        нужно указать вручную.
                                        """)));
    }
}
