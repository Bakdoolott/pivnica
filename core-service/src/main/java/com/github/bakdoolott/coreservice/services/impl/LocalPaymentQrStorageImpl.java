package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.services.PaymentQrStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalPaymentQrStorageImpl implements PaymentQrStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path basePath;

    public LocalPaymentQrStorageImpl(
            @Value("${storage.payment-qr-dir:./uploads/payment-qr}") String dir) {
        this.basePath = Paths.get(dir).toAbsolutePath();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать директорию для QR", e);
        }
    }

    @Override
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new LogicException("Файл QR обязателен");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new LogicException("Разрешённые форматы: " + ALLOWED_EXTENSIONS);
        }

        String filename = "qr-" + UUID.randomUUID() + "." + ext;
        Path target = basePath.resolve(filename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить QR", e);
        }
        return "/uploads/payment-qr/" + filename;
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) return;
        try {
            Files.deleteIfExists(basePath.resolve(Paths.get(url).getFileName()));
        } catch (IOException ignored) {
        }
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
