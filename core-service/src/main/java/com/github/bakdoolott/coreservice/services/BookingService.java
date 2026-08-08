package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingCreateDto dto);
}
