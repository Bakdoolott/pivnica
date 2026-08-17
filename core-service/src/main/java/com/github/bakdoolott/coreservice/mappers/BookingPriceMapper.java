package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.dto.PriceSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingPriceMapper {
    PriceResponse toResponse (BookingPrice price);
    PriceSummaryDto toSummary(BookingPrice price);
}
