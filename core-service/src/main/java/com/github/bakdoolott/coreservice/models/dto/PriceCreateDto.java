package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.TableType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PriceCreateDto {
    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    BigDecimal price;

    LocalDateTime effectiveFrom;

    @NotNull
    TableType tableType;
}
