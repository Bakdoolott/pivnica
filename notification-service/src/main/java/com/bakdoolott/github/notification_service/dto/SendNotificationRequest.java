package com.bakdoolott.github.notification_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description =
        """
        Запрос на отправку push-уведомления пользователю. \n
        Вызывается другими бэкенд-сервисами напрямую (не через gateway) \n
        у сервиса нет своего JWT пользователя, поэтому получатель указывается userId явно.
        """)
public record SendNotificationRequest(

        @Schema(
                description =
                        """
                        ID пользователя-получателя. Уведомление уйдёт на все его \n
                        зарегистрированные устройства из user_devices
                        """,
                example = "42",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "userId обязателен")
        Long userId,

        @Schema(
                description = "Заголовок push-уведомления",
                example = "Новое сообщение",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "title обязателен")
        String title,

        @Schema(
                description = "Текст push-уведомления",
                example = "Айгуль отправила вам сообщение",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "message обязателен")
        String message,

        @Schema(
                description =
                        """
                        Необязательный набор произвольных key-value полей, \n
                        например для навигации внутри приложения по клику на уведомление
                        """,
                example = "{\"chatId\": \"123\", \"screen\": \"chat\"}"
        )
        Map<String, String> data
) {}
