package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.dto.PriceSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.TableSummaryDto;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import com.github.bakdoolott.coreservice.models.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        String userName,
        String phoneNumber,
        List<TableSummaryDto> tables,
        LocalDateTime dateTime,
        LocalDateTime endsAt,
        Integer guestCount,
        String comment,
        LocalDateTime createdAt,
        BookingStatus bookingStatus,
        PaymentStatus paymentStatus,
        PriceSummaryDto price
){
    }
