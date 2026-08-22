package com.github.bakdoolott.coreservice.models.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingCancelResponse(
        Long bookingId,
        boolean cancellable,
        LocalDateTime cancelDeadline,
        BigDecimal totalAmount,
        BigDecimal refundAmount,
        int refundPercent,
        String message
) {
}
