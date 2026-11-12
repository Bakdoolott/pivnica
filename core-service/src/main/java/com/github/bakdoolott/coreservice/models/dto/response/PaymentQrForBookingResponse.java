package com.github.bakdoolott.coreservice.models.dto.response;

import java.math.BigDecimal;

public record PaymentQrForBookingResponse(
        Long bookingId,
        String imageUrl,
        String description,
        BigDecimal amountToPay,
        String messengerChatUrl
        ) {}