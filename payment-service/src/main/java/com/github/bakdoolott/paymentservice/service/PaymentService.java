package com.github.bakdoolott.paymentservice.service;

import com.github.bakdoolott.paymentservice.models.dto.request.CreatePaymentRequest;
import com.github.bakdoolott.paymentservice.models.dto.response.PaymentResponse;

public interface PaymentService {

    /**
     * Инициировать (сымитировать) оплату брони.
     * Создаёт платёж, определяет результат по мок-режиму и в той же транзакции
     * пишет событие в outbox для последующей публикации в Kafka.
     */
    PaymentResponse createPayment(Long userId, CreatePaymentRequest request);

    /** Получить платёж по id. */
    PaymentResponse getPayment(Long paymentId);
}
