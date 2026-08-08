package com.github.bakdoolott.paymentservice.controller;

import com.github.bakdoolott.paymentservice.models.dto.request.CreatePaymentRequest;
import com.github.bakdoolott.paymentservice.models.dto.response.PaymentResponse;
import com.github.bakdoolott.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment/payments")
@Tag(name = "Payments", description = "Мок-оплата бронирования столиков")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Инициировать оплату брони (мок)")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request,
                                                  Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        PaymentResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить статус платежа")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }
}
