package com.github.bakdoolott.coreservice.models.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentQrResponse(
        Long id,
        String imageUrl,
        String description,
        boolean enable,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy
) {}