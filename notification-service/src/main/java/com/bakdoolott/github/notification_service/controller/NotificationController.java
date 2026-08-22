package com.bakdoolott.github.notification_service.controller;

import com.bakdoolott.github.notification_service.dto.SendNotificationRequest;
import com.bakdoolott.github.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


//Точка входа для остальных сервисов присылаете userId + текст находим все зарегистрированные устройства
//этого пользователя и рассылаем на них push через Firebase.
//Вызывается напрямую, в обход gateway: gateway требует валидный JWT
//бэкенд-сервисы от своего имени, без пользовательского токена.
//Отправка асинхронная поэтому эндпоинт отвечает сразу: 202 — уведомление поставлено в очередь, 404 — у пользователя нет ни одного зарегистрированного устройства.

@RestController
@RequestMapping("/api/v1/notification/notifications")
@Tag(
        name = "Отправка уведомлений",
        description =
                """
                Server-to-server эндпоинт для других сервисов платформы. \n
                Вызывается напрямую по внутреннему адресу сервиса, в обход gateway.
                """
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @Operation(
            summary = "Отправить push-уведомление пользователю",
            description =
                    """
                    Ищет все устройства userId в user_devices и асинхронно \n
                    рассылает push через Firebase Cloud Messaging на каждое из них. \n
                    Мёртвые токены (устройство удалило приложение и т.п.) сервис \n
                    подчищает сам при неудачной отправке.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Уведомление поставлено в очередь на отправку"),
            @ApiResponse(responseCode = "400", description = "Не заполнены обязательные поля (userId/title/message)"),
            @ApiResponse(responseCode = "404", description = "У пользователя нет зарегистрированных устройств")
    })
    public ResponseEntity<?> send(@Valid @RequestBody SendNotificationRequest request) {
        boolean queued = notificationService.sendNotificationToUser(
                request.userId(),
                request.title(),
                request.message(),
                request.data()
        );

        if (!queued) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message", "У пользователя нет зарегистрированных устройств",
                            "userId", request.userId()
                    ));
        }

        return ResponseEntity.accepted()
                .body(Map.of("message", "Уведомление поставлено в очередь на отправку"));
    }
}
