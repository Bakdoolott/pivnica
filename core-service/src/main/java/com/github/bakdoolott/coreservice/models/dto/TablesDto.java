package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.TableState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TablesDto {
    Long id;

    Integer tableNumber;

    Integer x;

    Integer y;

    Integer placeCount;

    TableState tableState = TableState.AVAILABLE;

    Long hallId;

    boolean enable = true;
}
