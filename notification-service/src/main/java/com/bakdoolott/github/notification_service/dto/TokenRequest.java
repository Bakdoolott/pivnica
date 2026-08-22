package com.bakdoolott.github.notification_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Регистрация FCM-токена конкретного устройства за текущим пользователем")
public record TokenRequest(

        @Schema(
                description = "FCM-токен устройства, выданный Firebase SDK на клиенте",
                example = "dGhpc19pc19hX2Zha2VfZmNtX3Rva2Vu...",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "token обязателен")
        String token,

        @Schema(
                description = "Тип устройства/платформы",
                example = "ANDROID",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "deviceType обязателен")
        String deviceType
) {}
