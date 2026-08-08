package com.github.bakdoolott.paymentservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bakdoolott.paymentservice.config.PaymentProperties;
import com.github.bakdoolott.paymentservice.exceptions.NotFoundException;
import com.github.bakdoolott.paymentservice.models.OutboxEvent;
import com.github.bakdoolott.paymentservice.models.Payment;
import com.github.bakdoolott.paymentservice.models.dto.event.PaymentCompletedEvent;
import com.github.bakdoolott.paymentservice.models.dto.request.CreatePaymentRequest;
import com.github.bakdoolott.paymentservice.models.dto.response.PaymentResponse;
import com.github.bakdoolott.paymentservice.models.enums.OutboxStatus;
import com.github.bakdoolott.paymentservice.models.enums.PaymentStatus;
import com.github.bakdoolott.paymentservice.repositories.OutboxEventRepository;
import com.github.bakdoolott.paymentservice.repositories.PaymentRepository;
import com.github.bakdoolott.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String AGGREGATE_TYPE = "Payment";

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxRepository;
    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OutboxEventRepository outboxRepository,
                              PaymentProperties properties,
                              ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        // Идемпотентность: если пришёл повтор с тем же ключом — возвращаем существующий платёж.
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Идемпотентный повтор: возвращаю платёж id={} по ключу {}",
                        existing.get().getId(), request.getIdempotencyKey());
                return toResponse(existing.get());
            }
        }

        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency()
                : properties.getDefaultCurrency();

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setCurrency(currency);
        payment.setIdempotencyKey(request.getIdempotencyKey());

        // === МОК: решаем, "прошла" ли оплата ===
        PaymentStatus result = decideMockResult(request.getAmount());
        payment.setStatus(result);

        payment = paymentRepository.save(payment);
        log.info("Платёж id={} для booking={} обработан со статусом {}",
                payment.getId(), payment.getBookingId(), result);

        // Событие пишем в outbox в ЭТОЙ ЖЕ транзакции — гарантия, что оно не потеряется.
        writeOutboxEvent(payment);

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Платёж не найден: id=" + paymentId));
        return toResponse(payment);
    }

    /**
     * Мок-логика результата оплаты.
     *   ALWAYS_SUCCESS — всегда PAID.
     *   AMOUNT_BASED   — сумма кратна 100 -> PAID, иначе -> FAILED (для проверки ветки отказа).
     */
    private PaymentStatus decideMockResult(BigDecimal amount) {
        if ("AMOUNT_BASED".equalsIgnoreCase(properties.getMockMode())) {
            boolean divisibleBy100 = amount.remainder(BigDecimal.valueOf(100))
                    .compareTo(BigDecimal.ZERO) == 0;
            return divisibleBy100 ? PaymentStatus.PAID : PaymentStatus.FAILED;
        }
        return PaymentStatus.PAID;
    }

    private void writeOutboxEvent(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                PaymentCompletedEvent.EVENT_TYPE,
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                LocalDateTime.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            // В одной транзакции с платежом: если сериализация упала — откатываем всё.
            throw new IllegalStateException("Не удалось сериализовать событие оплаты", e);
        }

        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateType(AGGREGATE_TYPE);
        outbox.setAggregateId(String.valueOf(payment.getId()));
        outbox.setEventType(PaymentCompletedEvent.EVENT_TYPE);
        outbox.setTopic(properties.getEventsTopic());
        outbox.setPayload(payload);
        outbox.setStatus(OutboxStatus.PENDING);

        outboxRepository.save(outbox);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
