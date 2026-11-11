package com.github.bakdoolott.coreservice.services.impl;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
@Slf4j
@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не должен быть пустым");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Разрешена загрузка только изображений");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Допустимые форматы: jpg, jpeg, png, webp");
        }

        String newFilename = UUID.randomUUID() + "." + extension.toLowerCase();
        try {
            Files.createDirectories(rootLocation);
            file.transferTo(resolveSafe(newFilename));
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafe(filename));
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Не удалось удалить файл {}", filename, e);
        }
    }

    private Path resolveSafe(String filename) {
        Path path = rootLocation.resolve(filename).normalize();
        if (!path.startsWith(rootLocation)) {
            throw new IllegalArgumentException("Недопустимое имя файла");
        }
        return path;
    }
}


