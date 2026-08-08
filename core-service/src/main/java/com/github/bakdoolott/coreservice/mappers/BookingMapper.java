package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.PriceSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.TableSummaryDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = BookingPriceMapper.class,unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookingMapper {
    @Mapping(target = "tables", source = "tables")
    @Mapping(target = "price", source = "price")
    BookingResponse toResponse(Booking booking);

    TableSummaryDto toTableSummary(Tables table);

    default List<TableSummaryDto> toTableSummaryList(Set<Tables> tables){
        if (tables == null){
            return List.of();
        }
        return tables.stream()
                .sorted(Comparator.comparing(Tables::getId))
                .map(this::toTableSummary)
                .toList();
    }
}
