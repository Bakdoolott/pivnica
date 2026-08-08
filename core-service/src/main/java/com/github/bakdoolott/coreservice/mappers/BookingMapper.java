package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.PriceSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.TableSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingResponse toResponse(Booking booking);
    TableSummaryDto toTableSummary(Tables table);
}
