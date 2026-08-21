package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.enums.DayType;

import java.time.LocalDate;

public record SpecialDayResponse(
        Long id,
        LocalDate date,
        DayType dayType,
        String name,
        boolean closed
) {
}
