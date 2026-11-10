package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrForBookingResponse;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrResponse;
import com.github.bakdoolott.coreservice.services.PaymentQrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/payment-qr")
@Tag(name = "QR-код для оплаты")
public class PaymentQrPublicController {

    private final PaymentQrService service;

    @GetMapping("/for-booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "QR + сумма для конкретной брони",
            description = "Возвращает QR-картинку и сумму, которую нужно оплатить по этой брони")
    public ResponseEntity<PaymentQrForBookingResponse> getForBooking(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(service.getForBooking(userId, bookingId));
    }
}