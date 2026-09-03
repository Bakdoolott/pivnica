package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.enums.TableType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        Long id,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        TableType tableType
) {
}
