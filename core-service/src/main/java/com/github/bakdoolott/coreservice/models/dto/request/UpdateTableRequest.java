package com.github.bakdoolott.coreservice.models.dto.request;

import com.github.bakdoolott.coreservice.models.enums.TableState;

public record UpdateTableRequest(
        Long id,
        Integer tableNumber,
        Integer x,
        Integer y,
        Integer placeCount,
        TableState tableState,
        Long hallId
) {
}
