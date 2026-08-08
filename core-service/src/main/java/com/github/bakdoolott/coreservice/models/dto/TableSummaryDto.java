package com.github.bakdoolott.coreservice.models.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableSummaryDto {
    Long id;
    Integer tableNumber;
    Integer placeCount;
    Integer x;
    Integer y;
}