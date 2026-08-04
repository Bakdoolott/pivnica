package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.TableOnDateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TableMapper {
    @Mapping(target = "tableStatus", ignore = true)
    TableOnDateDto tableToTableOnDateDto(Tables tables);

    List<TableOnDateDto> tablesToTableOnDateDtoList(List<Tables> tables);
}
