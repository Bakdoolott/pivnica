package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.enums.TableState;

public record TableResponse(
        Long id,
        Integer tableNumber,
        Integer x,
        Integer y,
        Integer placeCount,
        TableState tableState,
        Long hallId
) {
}
