package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.dto.PriceCreateDto;
import com.github.bakdoolott.coreservice.models.dto.PriceUpdateDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;
import com.github.bakdoolott.coreservice.models.enums.TableType;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public interface BookingPriceService {
    PriceResponse createPrice(PriceCreateDto dto);
    PriceResponse updatePrice (PriceUpdateDto dto);

    Map<TableType, BookingPrice> getActivePricesAt(LocalDateTime at);

    BookingPrice getPriceEntityAt(TableType tableType,LocalDateTime at);

    List<PriceResponse> getCurrentPrices();

    PriceResponse getCurrentPrice(TableType tableType);

    List<PriceResponse> getHistory(TableType tableType);

}