package com.github.bakdoolott.coreservice.models.dto.request;

import com.github.bakdoolott.coreservice.models.enums.TableState;
import jakarta.validation.constraints.NotNull;

public record CreateTableRequest(
        @NotNull Integer tableNumber,
        @NotNull Integer x,
        @NotNull Integer y,
        @NotNull Integer placeCount,
        @NotNull TableState tableState,
        @NotNull Long hallId
) {}
