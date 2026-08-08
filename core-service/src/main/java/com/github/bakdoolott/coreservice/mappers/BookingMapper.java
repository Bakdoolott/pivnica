package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.PriceSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.TableSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {BookingPriceMapper.class})
public interface BookingMapper {
    @Mapping(target = "tables", source = "tables")
    @Mapping(target = "price", source = "price")
    BookingResponse toResponse(Booking booking);

    TableSummaryDto toTableSummary(Tables table);

    List<TableSummaryDto> toTableSummaryList(Set<Tables> tables);
}
