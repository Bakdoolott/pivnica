package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.TableType;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PriceUpdateDto {

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    BigDecimal price;

    @Future
    LocalDateTime effectiveFrom;

    @NotNull
    TableType tableType;
}