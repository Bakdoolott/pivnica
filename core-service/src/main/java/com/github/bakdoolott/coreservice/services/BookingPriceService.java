package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.dto.PriceCreateDto;
import com.github.bakdoolott.coreservice.models.dto.PriceUpdateDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingPriceService {
    PriceResponse createPrice(PriceCreateDto dto);
    PriceResponse updatePrice(PriceUpdateDto dto);
    BookingPrice getCurrentPriceEntity();
    BookingPrice getPriceAt(LocalDateTime dateTime);
    BigDecimal getCurrentPrice();
}
