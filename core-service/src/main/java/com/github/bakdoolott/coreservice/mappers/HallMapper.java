package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.dto.response.HallResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HallMapper {
    HallDto toDto(Hall hall);
    Hall toEntity(HallDto dto);
    HallResponse toResponse(Hall hall);
    HallResponse toResponse(HallDto dto);
    List<HallResponse> toResponseList(List<Hall> halls);
}
