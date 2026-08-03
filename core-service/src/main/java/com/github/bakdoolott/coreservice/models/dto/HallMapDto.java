package com.github.bakdoolott.coreservice.models.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HallMapDto {
    Long hallId;
    String hallNumber;
    Integer floor;
    List<TableOnDateDto> tableOnDateDtos;
}
