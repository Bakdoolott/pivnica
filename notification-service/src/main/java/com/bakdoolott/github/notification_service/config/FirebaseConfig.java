package com.bakdoolott.github.notification_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream(credentialsPath)) {

            if (serviceAccount == null) {
                log.error("Не найден файл учётных данных Firebase '{}' в classpath. " +
                        "Положите firebase-service-account.json в src/main/resources " +
                        "(или укажите другой путь через свойство firebase.credentials.path). " +
                        "Отправка push-уведомлений работать не будет, пока файл не появится.", credentialsPath);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase успешно инициализирован");
            }
        } catch (Exception e) {
            log.error("Не удалось инициализировать Firebase: {}", e.getMessage(), e);
        }
    }
}