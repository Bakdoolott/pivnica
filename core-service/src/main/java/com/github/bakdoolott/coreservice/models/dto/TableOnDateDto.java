package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.TableStatus;
import com.github.bakdoolott.coreservice.models.enums.TableType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableOnDateDto {
    Long id;
    Integer tableNumber;
    Integer x;
    Integer y;
    TableType tableType;
    TableStatus tableStatus;
}
