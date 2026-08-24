package com.bakdoolott.github.notification_service.service;

import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final FcmTokenService fcmTokenService;

    public NotificationService(FcmTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    /**
     * Главная точка входа: находит все устройства пользователя и рассылает на них уведомление.
     *
     * @return false, если у пользователя нет ни одного зарегистрированного устройства
     *         (отправлять некуда), true — если отправка поставлена в очередь.
     */
    public boolean sendNotificationToUser(Long userId, String title, String body, Map<String, String> data) {
        List<String> tokens = fcmTokenService.getDeviceTokensByUserId(userId);

        if (tokens.isEmpty()) {
            log.warn("У пользователя {} нет зарегистрированных устройств, уведомление не отправлено", userId);
            return false;
        }

        if (tokens.size() == 1) {
            sendPushNotification(tokens.get(0), title, body, data);
        } else {
            sendToMultipleDevices(tokens, title, body, data);
        }
        return true;
    }

    @Async("fcmExecutor")
    public void sendPushNotification(String targetToken, String title, String body) {
        sendPushNotification(targetToken, title, body, null);
    }

    @Async("fcmExecutor")
    public void sendPushNotification(String targetToken, String title, String body, Map<String, String> data) {
        Message.Builder messageBuilder = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        Message message = messageBuilder.build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Успешно отправлено: {}", response);

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED ||
                    errorCode == MessagingErrorCode.INVALID_ARGUMENT) {

                log.warn("Токен неактивен, удаляем из БД: {}", targetToken);
                fcmTokenService.removeToken(targetToken); // Удаляем из PostgreSQL
            } else {
                log.error("Неизвестная ошибка Firebase: {}", e.getMessage());
            }
        }
    }

    @Async("fcmExecutor")
    public void sendToMultipleDevices(List<String> tokens, String title, String body) {
        sendToMultipleDevices(tokens, title, body, null);
    }

    @Async("fcmExecutor")
    public void sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        MulticastMessage message = messageBuilder.build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException e = responses.get(i).getException();
                        MessagingErrorCode errorCode = e.getMessagingErrorCode();

                        if (errorCode == MessagingErrorCode.UNREGISTERED ||
                                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                            failedTokens.add(tokens.get(i));
                        }
                    }
                }

                if (!failedTokens.isEmpty()) {
                    failedTokens.forEach(fcmTokenService::removeToken);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Критическая ошибка при массовой отправке: {}", e.getMessage());
        }
    }
}