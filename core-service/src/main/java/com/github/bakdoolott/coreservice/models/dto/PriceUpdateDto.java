package com.github.bakdoolott.coreservice.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PriceUpdateDto {

    @NotNull
    @Positive
    BigDecimal price;

    LocalDateTime effectiveFrom;
}