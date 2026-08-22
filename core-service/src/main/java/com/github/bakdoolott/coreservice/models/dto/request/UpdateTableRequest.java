package com.github.bakdoolott.coreservice.models.dto.request;

import com.github.bakdoolott.coreservice.models.enums.TableState;
import com.github.bakdoolott.coreservice.models.enums.TableType;

public record UpdateTableRequest(
        Long id,
        Integer tableNumber,
        Integer x,
        Integer y,
        TableType tableType,
        TableState tableState,
        Long hallId
) {
}
