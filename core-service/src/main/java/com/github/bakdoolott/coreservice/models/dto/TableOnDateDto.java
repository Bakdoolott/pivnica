package com.github.bakdoolott.coreservice.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.bakdoolott.coreservice.models.enums.TableState;
import com.github.bakdoolott.coreservice.models.enums.TableStatus;
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
    Integer placeCount;
    TableStatus tableStatus;
    @JsonIgnore
    TableState state;
}
