package com.github.bakdoolott.coreservice.models.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PriceCreateDto {
    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    BigDecimal price;
}
