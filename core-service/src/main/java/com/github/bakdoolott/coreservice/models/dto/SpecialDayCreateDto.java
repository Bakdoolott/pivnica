package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.DayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SpecialDayCreateDto (
        @NotNull
        LocalDate date,

        @NotNull
        DayType dayType,

        @NotBlank
        @Size(max = 50)
        String name,

        boolean closed
){
}
