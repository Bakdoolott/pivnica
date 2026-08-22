package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.enums.TableState;
import com.github.bakdoolott.coreservice.models.enums.TableType;

public record TableResponse(
        Long id,
        Integer tableNumber,
        Integer x,
        Integer y,
        TableType tableType,
        TableState tableState,
        Long hallId
) {
}
