package com.github.bakdoolott.coreservice.models.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        Long id,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
