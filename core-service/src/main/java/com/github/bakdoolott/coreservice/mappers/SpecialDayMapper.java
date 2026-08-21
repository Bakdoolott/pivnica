package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.SpecialDay;
import com.github.bakdoolott.coreservice.models.dto.SpecialDayCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.SpecialDayResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpecialDayMapper {
    SpecialDay toEntity(SpecialDayCreateDto dto);

    SpecialDayResponse toResponse(SpecialDay entity);
}
