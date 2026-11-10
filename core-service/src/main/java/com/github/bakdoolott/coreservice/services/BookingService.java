package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.BookingCancelDto;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingCancelResponse;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingCreateDto dto);

    List<BookingResponse> getBookingsForNight(LocalDate date);

    BookingResponse cancelBooking(Long adminId, Long bookingId, BookingCancelDto dto);

    byte[] exportBookingsForNight(LocalDate date);

    BookingCancelResponse requestRefund (Long userId, Long bookingId);

    BookingCancelResponse getCancelResponse(Long userId, Long bookingId);

    BookingResponse cancelOwnBooking(Long userId, Long bookingId, String reason);

    BookingResponse markNoShow(Long adminId, Long bookingId);

    BookingResponse markPaymentSent(Long userId, Long bookingId);

    List<BookingResponse> getAwaitingConfirmation();

    BookingResponse confirmByAdmin(Long adminId, Long bookingId);

    BookingResponse rejectByAdmin(Long adminId, Long bookingId, String reason, boolean moneyReceived);

    BookingResponse markArrived(Long adminId, Long bookingId);

    BookingResponse markDeparted(Long adminId, Long bookingId);

    List<BookingResponse> getRefundRequests();

    BookingResponse markRefunded(Long adminId, Long bookingId);
}
