package com.github.bakdoolott.coreservice.services;

import org.springframework.web.multipart.MultipartFile;

public interface PaymentQrStorage {
    String save(MultipartFile file);
    void delete(String url);
}
