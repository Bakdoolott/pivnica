package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HallMapper {
    HallDto toDto(Hall hall);
    Hall toEntity(HallDto dto);
}
