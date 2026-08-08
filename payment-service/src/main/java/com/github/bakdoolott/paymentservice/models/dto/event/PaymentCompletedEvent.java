package com.github.bakdoolott.paymentservice.models.dto.event;

import com.github.bakdoolott.paymentservice.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Событие, публикуемое в Kafka после обработки оплаты.
 * Это контракт между payment-service и потребителями (core-service, notification-service).
 * Менять поля нужно осторожно и согласованно с командой.
 */
public record PaymentCompletedEvent(
        String eventId,
        String eventType,
        Long paymentId,
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime occurredAt
) {
    public static final String EVENT_TYPE = "PaymentCompleted";
}
