package com.github.bakdoolott.coreservice.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
@Service
public class FileStorageService {
    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    public String store(MultipartFile file) {
        // 1. Проверка на пустой файл
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не должен быть пустым");
            // Замените на ваш кастомный Exception, который обрабатывается в GlobalExceptionHandler
        }

        // 2. Проверка, что файл является изображением
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Разрешена загрузка только изображений");
        }

        try {
            // 3. Достаем расширение из оригинального имени
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 4. Собираем новое имя: UUID + расширение
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. Создаем директорию, если она еще не существует
            Files.createDirectories(this.rootLocation);

            // 6. Копируем файл на диск
            Path destinationFile = this.rootLocation.resolve(newFilename).toAbsolutePath().normalize();
            file.transferTo(destinationFile);

            // 7. Возвращаем новое имя
            return newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }
}


