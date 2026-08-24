package com.bakdoolott.github.notification_service.controller;

import com.bakdoolott.github.notification_service.dto.TokenRequest;
import com.bakdoolott.github.notification_service.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification/tokens")
@Tag(
        name = "FCM-токены устройств",
        description =
                """
                Вызывается мобильным клиентом через gateway сразу после логина, \n
                при обновлении FCM-токена (onNewToken) и перед логаутом. \n
                Требует X-User-Id — его подставляет gateway на основе валидного JWT.
                """
)
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    public FcmTokenController(FcmTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Зарегистрировать/обновить токен устройства",
            description =
                    """
                    Делает upsert по device_token: если такой токен уже был у кого-то \n
                    он просто переприкрепляется к текущему userId. \n
                    Нужно вызывать сразу после логина и при каждом обновлении FCM-токена на клиенте.
                    """
    )
    @SecurityRequirement(name = "X-User-Id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен сохранён/обновлён"),
            @ApiResponse(responseCode = "400", description = "Не заполнен token или deviceType"),
            @ApiResponse(responseCode = "400", description = "Отсутствует заголовок X-User-Id")
    })
    public ResponseEntity<?> registerToken(
            @Parameter(description = "ID текущего пользователя, подставляется gateway", required = true, example = "42")
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TokenRequest request) {

        fcmTokenService.saveOrUpdateToken(
                userId,
                request.token(),
                request.deviceType()
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unregister")
    @Operation(
            summary = "Отписать конкретное устройство",
            description =
                    """
                    Удаляет регистрацию только по переданному токену — на остальные \n
                    устройства пользователя (если он залогинен ещё где-то) не влияет. \n
                    Вызывать перед логаутом, пока JWT ещё не инвалидирован
                    """
    )
    @ApiResponse(responseCode = "200", description = "Токен удалён (или его и не было — идемпотентно)")
    public ResponseEntity<?> unregisterToken(
            @Parameter(description = "FCM-токен устройства, который нужно отписать", required = true)
            @RequestParam String token) {
        fcmTokenService.removeToken(token);
        return ResponseEntity.ok().build();
    }
}
