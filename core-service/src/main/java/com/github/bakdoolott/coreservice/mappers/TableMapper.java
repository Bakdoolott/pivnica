package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.TableOnDateDto;
import com.github.bakdoolott.coreservice.models.dto.TablesDto;
import com.github.bakdoolott.coreservice.models.dto.response.TableResponse;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TableMapper {
    @Mapping(target = "tableStatus", ignore = true)
    TableOnDateDto tableToTableOnDateDto(Tables tables);

    List<TableOnDateDto> tablesToTableOnDateDtoList(List<Tables> tables);

    @Mapping(
            source = "hall.id",
            target = "hallId"
    )
    TablesDto toDto(Tables entity);


    TableResponse toResponse(TablesDto entity);

    @InheritConfiguration
    Tables toEntity(TablesDto dto);

    @Mapping(
            source = "hall.id",
            target = "hallId"
    )
    TableResponse toResponse(Tables entity);

    List<TableResponse> toResponseList(List<Tables> tables);
}
